-- Flyway Migration V1: Create Notification Service Tables

-- Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    channel VARCHAR(50) NOT NULL, -- EMAIL, SMS, IN_APP, PUSH
    priority VARCHAR(50) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    metadata JSONB
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_created ON notifications (created_at);
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_priority ON notifications (priority);

-- Alert Rules Table
CREATE TABLE IF NOT EXISTS alert_rules (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    trigger_type VARCHAR(100) NOT NULL,
    conditions JSONB NOT NULL,
    actions JSONB NOT NULL,
    enabled BOOLEAN DEFAULT true,
    schedule VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alert_company ON alert_rules (company_id);
CREATE INDEX idx_alert_enabled ON alert_rules (enabled);
CREATE INDEX idx_alert_trigger ON alert_rules (trigger_type);

-- Notification Templates Table
CREATE TABLE IF NOT EXISTS notification_templates (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(100) NOT NULL UNIQUE,
    template_name VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    body TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    variables TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_templates_type ON notification_templates (type);
CREATE INDEX idx_templates_channel ON notification_templates (channel);

-- Notification Log Table
CREATE TABLE IF NOT EXISTS notification_log (
    id VARCHAR(255) PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    retries INTEGER DEFAULT 0,
    gateway_response JSONB,
    FOREIGN KEY (notification_id) REFERENCES notifications(id)
);

CREATE INDEX idx_log_notification ON notification_log (notification_id);
CREATE INDEX idx_log_status ON notification_log (status);
CREATE INDEX idx_log_sent ON notification_log (sent_at);

-- User Notification Preferences Table
CREATE TABLE IF NOT EXISTS user_notification_preferences (
    user_id VARCHAR(255) PRIMARY KEY,
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    in_app_enabled BOOLEAN DEFAULT true,
    notification_types JSONB,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Queue Table (for batch processing)
CREATE TABLE IF NOT EXISTS notification_queue (
    id VARCHAR(255) PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL,
    priority INTEGER DEFAULT 5,
    scheduled_for TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attempts INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    status VARCHAR(50) DEFAULT 'QUEUED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id)
);

CREATE INDEX idx_queue_scheduled ON notification_queue (scheduled_for);
CREATE INDEX idx_queue_status ON notification_queue (status);
CREATE INDEX idx_queue_priority ON notification_queue (priority DESC);

-- Insert Sample Notification Templates
INSERT INTO notification_templates (id, type, template_name, subject, body, channel, variables)
VALUES
    ('TPL001', 'BATTERY_LOW', 'Battery Low Alert', 'Low Battery Alert - {{vehicleName}}',
     'Your vehicle {{vehicleName}} battery is at {{batteryLevel}}%. Please charge soon.', 'EMAIL',
     ARRAY['vehicleName', 'batteryLevel']),

    ('TPL002', 'MAINTENANCE_DUE', 'Maintenance Due Reminder', 'Maintenance Due - {{vehicleName}}',
     'Your vehicle {{vehicleName}} is due for {{serviceType}} on {{dueDate}}.', 'EMAIL',
     ARRAY['vehicleName', 'serviceType', 'dueDate']),

    ('TPL003', 'CHARGING_COMPLETE', 'Charging Completed', 'Charging Complete - {{vehicleName}}',
     'Your vehicle {{vehicleName}} charging is complete. Energy consumed: {{energyKwh}} kWh, Cost: {{cost}}', 'PUSH',
     ARRAY['vehicleName', 'energyKwh', 'cost']),

    ('TPL004', 'DRIVER_BEHAVIOR_ALERT', 'Driver Behavior Alert', 'Driver Behavior Alert - {{driverName}}',
     'Driver {{driverName}} has {{incidentType}} incident. Please review.', 'EMAIL',
     ARRAY['driverName', 'incidentType']),

    ('TPL005', 'TRIP_SUMMARY', 'Trip Summary', 'Trip Summary - {{vehicleName}}',
     'Trip completed for {{vehicleName}}. Distance: {{distance}} km, Duration: {{duration}}.', 'IN_APP',
     ARRAY['vehicleName', 'distance', 'duration'])
ON CONFLICT (id) DO NOTHING;

-- Insert Sample Alert Rules
INSERT INTO alert_rules (id, company_id, rule_name, trigger_type, conditions, actions, enabled)
VALUES
    ('RULE001', 'COMP001', 'Low Battery Alert', 'BATTERY_LOW',
     '{"batteryLevel": {"operator": "lt", "value": 20}}'::jsonb,
     '{"notifyUser": true, "channels": ["EMAIL", "PUSH"], "priority": "HIGH"}'::jsonb, true),

    ('RULE002', 'COMP001', 'Maintenance Overdue', 'MAINTENANCE_DUE',
     '{"daysPastDue": {"operator": "gte", "value": 0}}'::jsonb,
     '{"notifyUser": true, "channels": ["EMAIL", "SMS"], "priority": "HIGH"}'::jsonb, true),

    ('RULE003', 'COMP001', 'Harsh Driving Detected', 'DRIVER_BEHAVIOR_ALERT',
     '{"harshBraking": {"operator": "gt", "value": 5}}'::jsonb,
     '{"notifyManager": true, "channels": ["EMAIL"], "priority": "MEDIUM"}'::jsonb, true)
ON CONFLICT (id) DO NOTHING;

-- Insert Sample Notifications
INSERT INTO notifications (id, user_id, type, title, message, status, channel, priority, created_at)
VALUES
    ('NOT001', 'USR001', 'BATTERY_LOW', 'Low Battery Alert', 'Your vehicle VEH001 battery is at 15%. Please charge soon.', 'SENT', 'EMAIL', 'HIGH', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    ('NOT002', 'USR001', 'CHARGING_COMPLETE', 'Charging Complete', 'Your vehicle VEH001 charging is complete. Energy consumed: 45.5 kWh', 'SENT', 'PUSH', 'MEDIUM', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
    ('NOT003', 'USR002', 'MAINTENANCE_DUE', 'Maintenance Reminder', 'Your vehicle VEH002 is due for battery check on 2025-11-15.', 'SENT', 'EMAIL', 'MEDIUM', CURRENT_TIMESTAMP - INTERVAL '3 hours')
ON CONFLICT (id) DO NOTHING;

-- Update Timestamp Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_alert_rules_updated_at BEFORE UPDATE ON alert_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_templates_updated_at BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_preferences_updated_at BEFORE UPDATE ON user_notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
