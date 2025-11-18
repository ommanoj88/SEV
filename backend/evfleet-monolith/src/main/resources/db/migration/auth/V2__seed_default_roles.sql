-- Seed default roles
INSERT INTO roles (name, description, created_at, updated_at)
VALUES 
    ('ROLE_ADMIN', 'Administrator with full system access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_SUPER_ADMIN', 'Super Administrator with all permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_FLEET_MANAGER', 'Fleet Manager for managing vehicles and drivers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_DRIVER', 'Driver role for drivers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_MAINTENANCE_MANAGER', 'Maintenance Manager for vehicle maintenance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_ANALYST', 'Analyst for data analysis and reporting', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_SUPPORT', 'Support team member for customer support', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_USER', 'Regular user with basic access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
