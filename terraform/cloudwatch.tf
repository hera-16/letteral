# ========================================
# CloudWatch ダッシュボード
# ========================================

resource "aws_cloudwatch_dashboard" "letteral_main" {
  dashboard_name = "Letteral-Production-Dashboard"

  dashboard_body = jsonencode({
    widgets = [
      # ========================================
      # ALB メトリクス
      # ========================================
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/ApplicationELB", "RequestCount", { stat = "Sum", label = "総リクエスト数" }],
            [".", "TargetResponseTime", { stat = "Average", label = "平均レスポンスタイム" }],
            [".", "HTTPCode_Target_4XX_Count", { stat = "Sum", label = "4xxエラー" }],
            [".", "HTTPCode_Target_5XX_Count", { stat = "Sum", label = "5xxエラー" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "ALB - リクエスト & エラー"
          period  = 300
        }
      },
      # ========================================
      # ECS メトリクス
      # ========================================
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/ECS", "CPUUtilization", { dimensions = { ServiceName = "letteral-backend-service", ClusterName = "letteral-cluster" }, stat = "Average", label = "Backend CPU" }],
            [".", "MemoryUtilization", { dimensions = { ServiceName = "letteral-backend-service", ClusterName = "letteral-cluster" }, stat = "Average", label = "Backend Memory" }],
            [".", "CPUUtilization", { dimensions = { ServiceName = "letteral-frontend-service", ClusterName = "letteral-cluster" }, stat = "Average", label = "Frontend CPU" }],
            [".", "MemoryUtilization", { dimensions = { ServiceName = "letteral-frontend-service", ClusterName = "letteral-cluster" }, stat = "Average", label = "Frontend Memory" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "ECS - CPU & メモリ使用率"
          period  = 300
          yAxis = {
            left = {
              min = 0
              max = 100
            }
          }
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/ECS", "RunningTasksCount", { dimensions = { ServiceName = "letteral-backend-service", ClusterName = "letteral-cluster" }, stat = "Average", label = "Backend Tasks" }],
            [".", "RunningTasksCount", { dimensions = { ServiceName = "letteral-frontend-service", ClusterName = "letteral-cluster" }, stat = "Average", label = "Frontend Tasks" }]
          ]
          view    = "timeSeries"
          stacked = true
          region  = "ap-northeast-1"
          title   = "ECS - 実行中タスク数"
          period  = 300
        }
      },
      # ========================================
      # RDS メトリクス
      # ========================================
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", { dimensions = { DBInstanceIdentifier = "letteral-db" }, stat = "Average", label = "CPU使用率" }],
            [".", "DatabaseConnections", { dimensions = { DBInstanceIdentifier = "letteral-db" }, stat = "Average", label = "接続数" }],
            [".", "FreeableMemory", { dimensions = { DBInstanceIdentifier = "letteral-db" }, stat = "Average", label = "空きメモリ", yAxis = "right" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "RDS - パフォーマンス"
          period  = 300
          yAxis = {
            left = {
              label = "CPU / 接続数"
            }
            right = {
              label = "メモリ (Bytes)"
            }
          }
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/RDS", "ReadLatency", { dimensions = { DBInstanceIdentifier = "letteral-db" }, stat = "Average", label = "読み込みレイテンシー" }],
            [".", "WriteLatency", { dimensions = { DBInstanceIdentifier = "letteral-db" }, stat = "Average", label = "書き込みレイテンシー" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "RDS - レイテンシー"
          period  = 300
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/RDS", "FreeStorageSpace", { dimensions = { DBInstanceIdentifier = "letteral-db" }, stat = "Average", label = "空きストレージ" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "RDS - ストレージ"
          period  = 300
        }
      },
      # ========================================
      # ElastiCache Redis メトリクス
      # ========================================
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/ElastiCache", "CPUUtilization", { dimensions = { CacheClusterId = "letteral-redis" }, stat = "Average", label = "CPU使用率" }],
            [".", "DatabaseMemoryUsagePercentage", { dimensions = { CacheClusterId = "letteral-redis" }, stat = "Average", label = "メモリ使用率" }],
            [".", "CurrConnections", { dimensions = { CacheClusterId = "letteral-redis" }, stat = "Average", label = "接続数" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "Redis - パフォーマンス"
          period  = 300
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/ElastiCache", "CacheHits", { dimensions = { CacheClusterId = "letteral-redis" }, stat = "Sum", label = "キャッシュヒット" }],
            [".", "CacheMisses", { dimensions = { CacheClusterId = "letteral-redis" }, stat = "Sum", label = "キャッシュミス" }]
          ]
          view    = "timeSeries"
          stacked = true
          region  = "ap-northeast-1"
          title   = "Redis - キャッシュヒット/ミス"
          period  = 300
        }
      },
      # ========================================
      # CloudFront メトリクス
      # ========================================
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/CloudFront", "Requests", { stat = "Sum", label = "リクエスト数" }],
            [".", "BytesDownloaded", { stat = "Sum", label = "ダウンロードバイト数", yAxis = "right" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "us-east-1"  # CloudFrontはus-east-1
          title   = "CloudFront - トラフィック"
          period  = 300
        }
      },
      # ========================================
      # カスタムメトリクス (アプリケーションログ)
      # ========================================
      {
        type = "metric"
        properties = {
          metrics = [
            ["Letteral/Application", "ProgressPostCreated", { stat = "Sum", label = "進捗投稿数" }],
            [".", "UserLogin", { stat = "Sum", label = "ログイン数" }],
            [".", "APIErrors", { stat = "Sum", label = "APIエラー数" }]
          ]
          view    = "timeSeries"
          stacked = false
          region  = "ap-northeast-1"
          title   = "アプリケーション - ビジネスメトリクス"
          period  = 300
        }
      },
      # ========================================
      # ログインサイト
      # ========================================
      {
        type = "log"
        properties = {
          query   = <<-EOT
            SOURCE '/aws/ecs/letteral-backend'
            | fields @timestamp, @message
            | filter @message like /ERROR/
            | stats count() by bin(5m)
          EOT
          region  = "ap-northeast-1"
          title   = "エラーログ (5分間隔)"
        }
      }
    ]
  })
}

# ========================================
# CloudWatch アラーム
# ========================================

# SNSトピック (アラート通知用)
resource "aws_sns_topic" "letteral_alerts" {
  name = "letteral-production-alerts"
}

resource "aws_sns_topic_subscription" "email_alerts" {
  topic_arn = aws_sns_topic.letteral_alerts.arn
  protocol  = "email"
  endpoint  = "ops@example.com"  # 運用チームのメールアドレスに変更
}

# ALB - 高レスポンスタイム
resource "aws_cloudwatch_metric_alarm" "alb_high_response_time" {
  alarm_name          = "letteral-alb-high-response-time"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = 300
  statistic           = "Average"
  threshold           = 2.0  # 2秒以上
  alarm_description   = "ALBのレスポンスタイムが2秒を超えています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    LoadBalancer = "app/letteral-alb/xxx"  # 実際のALB ARNに置き換え
  }
}

# ALB - 5xxエラー率
resource "aws_cloudwatch_metric_alarm" "alb_5xx_errors" {
  alarm_name          = "letteral-alb-5xx-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "HTTPCode_Target_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = 300
  statistic           = "Sum"
  threshold           = 10  # 5分間で10件以上
  alarm_description   = "ALBで5xxエラーが多発しています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    LoadBalancer = "app/letteral-alb/xxx"
  }
}

# ECS Backend - 高CPU使用率
resource "aws_cloudwatch_metric_alarm" "ecs_backend_high_cpu" {
  alarm_name          = "letteral-ecs-backend-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 80  # 80%以上
  alarm_description   = "Backend ECS タスクのCPU使用率が高くなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    ServiceName = "letteral-backend-service"
    ClusterName = "letteral-cluster"
  }
}

# ECS Backend - 高メモリ使用率
resource "aws_cloudwatch_metric_alarm" "ecs_backend_high_memory" {
  alarm_name          = "letteral-ecs-backend-high-memory"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 85  # 85%以上
  alarm_description   = "Backend ECS タスクのメモリ使用率が高くなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    ServiceName = "letteral-backend-service"
    ClusterName = "letteral-cluster"
  }
}

# ECS - タスク数異常
resource "aws_cloudwatch_metric_alarm" "ecs_backend_low_tasks" {
  alarm_name          = "letteral-ecs-backend-low-tasks"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "RunningTasksCount"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 2  # 最低2タスク必要
  alarm_description   = "Backend ECS タスクの実行数が期待値を下回っています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    ServiceName = "letteral-backend-service"
    ClusterName = "letteral-cluster"
  }
}

# RDS - 高CPU使用率
resource "aws_cloudwatch_metric_alarm" "rds_high_cpu" {
  alarm_name          = "letteral-rds-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDSのCPU使用率が高くなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    DBInstanceIdentifier = "letteral-db"
  }
}

# RDS - 接続数過多
resource "aws_cloudwatch_metric_alarm" "rds_high_connections" {
  alarm_name          = "letteral-rds-high-connections"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80  # max_connectionsの80%
  alarm_description   = "RDSの接続数が多くなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    DBInstanceIdentifier = "letteral-db"
  }
}

# RDS - ストレージ容量不足
resource "aws_cloudwatch_metric_alarm" "rds_low_storage" {
  alarm_name          = "letteral-rds-low-storage"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 10737418240  # 10GB
  alarm_description   = "RDSのストレージ容量が少なくなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    DBInstanceIdentifier = "letteral-db"
  }
}

# Redis - 高CPU使用率
resource "aws_cloudwatch_metric_alarm" "redis_high_cpu" {
  alarm_name          = "letteral-redis-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = 75
  alarm_description   = "RedisのCPU使用率が高くなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    CacheClusterId = "letteral-redis"
  }
}

# Redis - 高メモリ使用率
resource "aws_cloudwatch_metric_alarm" "redis_high_memory" {
  alarm_name          = "letteral-redis-high-memory"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseMemoryUsagePercentage"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = 90
  alarm_description   = "Redisのメモリ使用率が高くなっています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    CacheClusterId = "letteral-redis"
  }
}

# カスタムメトリクス - APIエラー率
resource "aws_cloudwatch_metric_alarm" "api_high_error_rate" {
  alarm_name          = "letteral-api-high-error-rate"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "APIErrors"
  namespace           = "Letteral/Application"
  period              = 300
  statistic           = "Sum"
  threshold           = 50  # 5分間で50件以上
  alarm_description   = "APIエラーが多発しています"
  alarm_actions       = [aws_sns_topic.letteral_alerts.arn]
}

# ========================================
# CloudWatch Logs Insights - 保存されたクエリ
# ========================================

resource "aws_cloudwatch_query_definition" "error_logs" {
  name = "Letteral - エラーログ分析"

  log_group_names = [
    "/aws/ecs/letteral-backend",
    "/aws/ecs/letteral-frontend"
  ]

  query_string = <<-EOT
    fields @timestamp, @message, @logStream
    | filter @message like /ERROR/ or @message like /Exception/
    | sort @timestamp desc
    | limit 100
  EOT
}

resource "aws_cloudwatch_query_definition" "slow_queries" {
  name = "Letteral - スロークエリ"

  log_group_names = [
    "/aws/ecs/letteral-backend"
  ]

  query_string = <<-EOT
    fields @timestamp, @message
    | filter @message like /Slow query/
    | parse @message /duration: *ms/ as duration
    | sort duration desc
    | limit 50
  EOT
}

resource "aws_cloudwatch_query_definition" "user_activity" {
  name = "Letteral - ユーザーアクティビティ"

  log_group_names = [
    "/aws/ecs/letteral-backend"
  ]

  query_string = <<-EOT
    fields @timestamp, @message
    | filter @message like /POST \/api\/progress/ or @message like /GET \/api\/progress/
    | stats count() by bin(5m)
  EOT
}

# ========================================
# アラーム復旧通知
# ========================================

resource "aws_cloudwatch_metric_alarm" "alb_high_response_time_ok" {
  alarm_name          = "letteral-alb-response-time-ok"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = 300
  statistic           = "Average"
  threshold           = 1.0
  alarm_description   = "ALBのレスポンスタイムが正常に戻りました"
  ok_actions          = [aws_sns_topic.letteral_alerts.arn]

  dimensions = {
    LoadBalancer = "app/letteral-alb/xxx"
  }
}

# ========================================
# 出力
# ========================================

output "cloudwatch_dashboard_url" {
  value = "https://console.aws.amazon.com/cloudwatch/home?region=ap-northeast-1#dashboards:name=${aws_cloudwatch_dashboard.letteral_main.dashboard_name}"
}

output "sns_topic_arn" {
  value = aws_sns_topic.letteral_alerts.arn
}
