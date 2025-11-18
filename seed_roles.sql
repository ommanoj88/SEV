-- Seed default roles for EVFleet application
-- This should match V2__seed_default_roles.sql from the microservices

-- Clear existing roles (if any)
TRUNCATE TABLE roles CASCADE;

-- Insert default roles
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
(1, 'ROLE_USER', 'Standard user with basic access', NOW(), NOW()),
(2, 'ROLE_ADMIN', 'Administrator with full access', NOW(), NOW()),
(3, 'ROLE_FLEET_MANAGER', 'Fleet manager with vehicle management access', NOW(), NOW()),
(4, 'ROLE_DRIVER', 'Driver with limited access to assigned vehicles', NOW(), NOW()),
(5, 'ROLE_ANALYST', 'Analyst with read-only access to analytics', NOW(), NOW());

-- Reset sequence
SELECT setval('roles_id_seq', 5, true);

-- Verify
SELECT * FROM roles;
