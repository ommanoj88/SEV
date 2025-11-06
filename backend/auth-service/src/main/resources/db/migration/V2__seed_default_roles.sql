-- Insert default roles
INSERT INTO roles (name, description, permissions) VALUES
('SUPER_ADMIN', 'Super Administrator with full system access', '{"all": true}'),
('ADMIN', 'Administrator with most system access', '{"users": ["read", "create", "update", "delete"], "fleet": ["read", "create", "update", "delete"], "reports": ["read"]}'),
('FLEET_MANAGER', 'Fleet Manager with fleet management access', '{"fleet": ["read", "create", "update"], "vehicles": ["read", "create", "update"], "drivers": ["read", "create", "update"], "reports": ["read"]}'),
('DRIVER', 'Driver with limited access', '{"vehicles": ["read"], "trips": ["read", "create", "update"]}'),
('VIEWER', 'Read-only viewer', '{"fleet": ["read"], "vehicles": ["read"], "reports": ["read"]}')
ON CONFLICT (name) DO NOTHING;
