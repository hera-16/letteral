-- Add test user for E2E testing
-- Username: testuser
-- Email: testuser@example.com
-- Password: password
-- BCrypt hash for "password": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (username, email, password, display_name, tenant_id, primary_organization_id, created_at)
VALUES (
  'testuser',
  'testuser@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'Test User',
  1,
  1,
  NOW()
);
