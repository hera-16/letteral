# ==========================================
# Letteral AWS Infrastructure - Terraform Main Configuration
# ==========================================

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # S3 Backend for State Management
  backend "s3" {
    bucket         = "letteral-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "ap-northeast-1"
    encrypt        = true
    dynamodb_table = "letteral-terraform-lock"
  }
}

# ==========================================
# Provider Configuration
# ==========================================
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Letteral"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# ==========================================
# Data Sources
# ==========================================
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# ==========================================
# Modules
# ==========================================

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  environment         = var.environment
  vpc_cidr            = var.vpc_cidr
  availability_zones  = slice(data.aws_availability_zones.available.names, 0, 3)
  public_subnet_cidrs = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  database_subnet_cidrs = var.database_subnet_cidrs
}

# Security Groups Module
module "security_groups" {
  source = "./modules/security_groups"

  environment = var.environment
  vpc_id      = module.vpc.vpc_id
}

# RDS Module
module "rds" {
  source = "./modules/rds"

  environment             = var.environment
  vpc_id                  = module.vpc.vpc_id
  database_subnet_ids     = module.vpc.database_subnet_ids
  rds_security_group_id   = module.security_groups.rds_security_group_id
  db_instance_class       = var.db_instance_class
  db_allocated_storage    = var.db_allocated_storage
  db_name                 = var.db_name
  db_username             = var.db_username
  db_password             = var.db_password
  multi_az                = var.rds_multi_az
  backup_retention_period = var.backup_retention_period
}

# ElastiCache Redis Module
module "elasticache" {
  source = "./modules/elasticache"

  environment              = var.environment
  vpc_id                   = module.vpc.vpc_id
  subnet_ids               = module.vpc.private_subnet_ids
  redis_security_group_id  = module.security_groups.redis_security_group_id
  node_type                = var.redis_node_type
  num_cache_nodes          = var.redis_num_cache_nodes
}

# ALB Module
module "alb" {
  source = "./modules/alb"

  environment           = var.environment
  vpc_id                = module.vpc.vpc_id
  public_subnet_ids     = module.vpc.public_subnet_ids
  alb_security_group_id = module.security_groups.alb_security_group_id
  certificate_arn       = var.acm_certificate_arn
}

# ECS Cluster Module
module "ecs" {
  source = "./modules/ecs"

  environment            = var.environment
  vpc_id                 = module.vpc.vpc_id
  private_subnet_ids     = module.vpc.private_subnet_ids
  ecs_security_group_id  = module.security_groups.ecs_security_group_id
  alb_target_group_arn   = module.alb.target_group_arn

  # Task Configuration
  task_cpu               = var.ecs_task_cpu
  task_memory            = var.ecs_task_memory
  desired_count          = var.ecs_desired_count
  min_capacity           = var.ecs_min_capacity
  max_capacity           = var.ecs_max_capacity

  # Environment Variables
  database_url           = module.rds.endpoint
  database_username      = var.db_username
  database_password      = var.db_password
  redis_endpoint         = module.elasticache.redis_endpoint
  jwt_secret_arn         = aws_secretsmanager_secret.jwt_secret.arn

  # ECR
  ecr_repository_url     = aws_ecr_repository.backend.repository_url
}

# S3 Buckets Module
module "s3" {
  source = "./modules/s3"

  environment = var.environment
}

# CloudFront Module
module "cloudfront" {
  source = "./modules/cloudfront"

  environment        = var.environment
  s3_bucket_id       = module.s3.static_assets_bucket_id
  s3_bucket_domain   = module.s3.static_assets_bucket_domain
  alb_domain_name    = module.alb.alb_dns_name
  certificate_arn    = var.acm_certificate_arn
  domain_name        = var.domain_name
}

# ==========================================
# ECR Repository
# ==========================================
resource "aws_ecr_repository" "backend" {
  name                 = "letteral-backend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "letteral-backend-${var.environment}"
  }
}

resource "aws_ecr_lifecycle_policy" "backend" {
  repository = aws_ecr_repository.backend.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# ==========================================
# Secrets Manager
# ==========================================
resource "aws_secretsmanager_secret" "jwt_secret" {
  name        = "letteral/${var.environment}/jwt-secret"
  description = "JWT Secret for Letteral ${var.environment}"

  tags = {
    Name = "letteral-jwt-secret-${var.environment}"
  }
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = var.jwt_secret
}

resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "letteral/${var.environment}/db-credentials"
  description = "Database credentials for Letteral ${var.environment}"

  tags = {
    Name = "letteral-db-credentials-${var.environment}"
  }
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
  })
}

# ==========================================
# CloudWatch Log Groups
# ==========================================
resource "aws_cloudwatch_log_group" "ecs_backend" {
  name              = "/aws/ecs/letteral-backend-${var.environment}"
  retention_in_days = 30

  tags = {
    Name = "letteral-backend-logs-${var.environment}"
  }
}

# ==========================================
# Outputs
# ==========================================
output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "alb_dns_name" {
  description = "ALB DNS Name"
  value       = module.alb.alb_dns_name
}

output "rds_endpoint" {
  description = "RDS Endpoint"
  value       = module.rds.endpoint
  sensitive   = true
}

output "redis_endpoint" {
  description = "Redis Endpoint"
  value       = module.elasticache.redis_endpoint
  sensitive   = true
}

output "cloudfront_domain" {
  description = "CloudFront Distribution Domain"
  value       = module.cloudfront.cloudfront_domain_name
}

output "ecr_repository_url" {
  description = "ECR Repository URL"
  value       = aws_ecr_repository.backend.repository_url
}
