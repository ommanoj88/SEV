-- V2__create_telemetry_alerts_table.sql
-- Flyway migration for telemetry alerts system
-- Supports deduplication and priority-based alert management

CREATE TABLE IF NOT EXISTS telemetry_alerts (
    id BIGSERIAL PRIMARY KEY,
    
    -- Foreign keys
    vehicle_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    driver_id BIGINT,
    
    -- Alert classification
    alert_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Alert content
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    
    -- Context data
    current_value DOUBLE PRECISION,
    threshold_value DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    
    -- Timestamps
    triggered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    acknowledged_by BIGINT,
    resolved_at TIMESTAMP,
    resolved_by BIGINT,
    resolution_notes VARCHAR(500),
    
    -- Notification tracking
    notification_sent BOOLEAN NOT NULL DEFAULT FALSE,
    notification_sent_at TIMESTAMP,
    
    -- Audit fields (from BaseEntity)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT chk_alert_type CHECK (alert_type IN (
        'LOW_BATTERY', 'CRITICAL_BATTERY', 'BATTERY_TEMPERATURE',
        'EXCESSIVE_SPEED', 'SUDDEN_ACCELERATION', 'HARSH_BRAKING',
        'GEOFENCE_ENTRY', 'GEOFENCE_EXIT', 'ROUTE_DEVIATION',
        'CONNECTION_LOST', 'CONNECTION_RESTORED', 'GPS_SIGNAL_LOST',
        'ENGINE_WARNING', 'MAINTENANCE_DUE', 'TIRE_PRESSURE',
        'LOW_FUEL', 'CHARGING_COMPLETE', 'CHARGING_INTERRUPTED',
        'CUSTOM'
    )),
    CONSTRAINT chk_alert_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_alert_status CHECK (status IN ('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'EXPIRED', 'SUPPRESSED'))
);

-- Performance indexes
CREATE INDEX idx_alert_vehicle ON telemetry_alerts(vehicle_id);
CREATE INDEX idx_alert_company ON telemetry_alerts(company_id);
CREATE INDEX idx_alert_type ON telemetry_alerts(alert_type);
CREATE INDEX idx_alert_priority ON telemetry_alerts(priority);
CREATE INDEX idx_alert_status ON telemetry_alerts(status);
CREATE INDEX idx_alert_created ON telemetry_alerts(created_at);
CREATE INDEX idx_alert_triggered ON telemetry_alerts(triggered_at);

-- Composite index for deduplication queries
CREATE INDEX idx_alert_dedup ON telemetry_alerts(vehicle_id, alert_type, status);

-- Composite index for urgent alerts query
CREATE INDEX idx_alert_urgent ON telemetry_alerts(company_id, status, priority) 
    WHERE status = 'ACTIVE' AND priority IN ('CRITICAL', 'HIGH');

-- Index for pending notifications
CREATE INDEX idx_alert_pending_notif ON telemetry_alerts(notification_sent, status) 
    WHERE notification_sent = FALSE AND status = 'ACTIVE';

-- Comments
COMMENT ON TABLE telemetry_alerts IS 'Stores telemetry-based alerts for vehicles';
COMMENT ON COLUMN telemetry_alerts.alert_type IS 'Type of alert (LOW_BATTERY, EXCESSIVE_SPEED, etc.)';
COMMENT ON COLUMN telemetry_alerts.priority IS 'Alert priority: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN telemetry_alerts.status IS 'Alert status: ACTIVE, ACKNOWLEDGED, RESOLVED, EXPIRED, SUPPRESSED';
COMMENT ON COLUMN telemetry_alerts.current_value IS 'The actual value that triggered the alert';
COMMENT ON COLUMN telemetry_alerts.threshold_value IS 'The threshold value that was exceeded';
