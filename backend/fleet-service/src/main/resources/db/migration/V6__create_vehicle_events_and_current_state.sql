-- V6__create_vehicle_events_and_current_state.sql
-- Creates tables for tracking vehicle production events and current state
-- Implements comprehensive event sourcing for vehicle operations

-- ========================================
-- VEHICLE EVENTS TABLE
-- ========================================
-- Tracks all production events for vehicles (historical record)
CREATE TABLE IF NOT EXISTS vehicle_events (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_subtype VARCHAR(50),
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    severity VARCHAR(20),
    
    -- Event details (JSON for flexibility)
    event_data JSONB,
    
    -- Location information
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_name VARCHAR(255),
    
    -- Related entities
    driver_id BIGINT,
    trip_id BIGINT,
    maintenance_id BIGINT,
    charging_session_id BIGINT,
    
    -- Event metrics
    battery_soc DOUBLE PRECISION,
    fuel_level DOUBLE PRECISION,
    odometer DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    
    -- Metadata
    source VARCHAR(50), -- e.g., 'TELEMETRY', 'MANUAL', 'SYSTEM', 'EXTERNAL'
    user_id BIGINT,
    company_id BIGINT NOT NULL,
    
    -- Additional context
    description TEXT,
    notes TEXT,
    
    -- Audit trail
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_vehicle_events_vehicle_id ON vehicle_events(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_events_event_type ON vehicle_events(event_type);
CREATE INDEX IF NOT EXISTS idx_vehicle_events_timestamp ON vehicle_events(event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_vehicle_events_vehicle_timestamp ON vehicle_events(vehicle_id, event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_vehicle_events_company_id ON vehicle_events(company_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_events_severity ON vehicle_events(severity) WHERE severity IN ('CRITICAL', 'HIGH');
CREATE INDEX IF NOT EXISTS idx_vehicle_events_event_data ON vehicle_events USING gin(event_data);

-- ========================================
-- VEHICLE CURRENT STATE TABLE
-- ========================================
-- Maintains the most recent state of each vehicle (snapshot)
CREATE TABLE IF NOT EXISTS vehicle_current_state (
    vehicle_id BIGINT PRIMARY KEY,
    
    -- Basic status
    status VARCHAR(20) NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Location
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_name VARCHAR(255),
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    
    -- Battery/Fuel status (supports both EV and ICE)
    battery_soc DOUBLE PRECISION,
    battery_health DOUBLE PRECISION,
    battery_temperature DOUBLE PRECISION,
    fuel_level DOUBLE PRECISION,
    
    -- Odometer and usage
    odometer DOUBLE PRECISION,
    total_distance DOUBLE PRECISION DEFAULT 0,
    total_energy_consumed DOUBLE PRECISION DEFAULT 0,
    total_fuel_consumed DOUBLE PRECISION DEFAULT 0,
    
    -- Current activity
    current_driver_id BIGINT,
    current_trip_id BIGINT,
    is_charging BOOLEAN DEFAULT FALSE,
    is_in_maintenance BOOLEAN DEFAULT FALSE,
    is_in_trip BOOLEAN DEFAULT FALSE,
    
    -- Charging info
    charging_station_id BIGINT,
    charging_started_at TIMESTAMP,
    estimated_charging_completion TIMESTAMP,
    
    -- Maintenance info
    last_maintenance_date TIMESTAMP,
    next_maintenance_due_date TIMESTAMP,
    maintenance_status VARCHAR(50),
    
    -- Alert status
    active_alerts_count INTEGER DEFAULT 0,
    critical_alerts_count INTEGER DEFAULT 0,
    last_alert_timestamp TIMESTAMP,
    
    -- Connection status
    is_connected BOOLEAN DEFAULT TRUE,
    last_telemetry_received TIMESTAMP,
    signal_strength INTEGER,
    
    -- Performance metrics
    average_speed_last_trip DOUBLE PRECISION,
    efficiency_score DOUBLE PRECISION,
    
    -- Company reference
    company_id BIGINT NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_vehicle_current_state_status ON vehicle_current_state(status);
CREATE INDEX IF NOT EXISTS idx_vehicle_current_state_company_id ON vehicle_current_state(company_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_current_state_is_charging ON vehicle_current_state(is_charging) WHERE is_charging = TRUE;
CREATE INDEX IF NOT EXISTS idx_vehicle_current_state_is_in_maintenance ON vehicle_current_state(is_in_maintenance) WHERE is_in_maintenance = TRUE;
CREATE INDEX IF NOT EXISTS idx_vehicle_current_state_is_in_trip ON vehicle_current_state(is_in_trip) WHERE is_in_trip = TRUE;
CREATE INDEX IF NOT EXISTS idx_vehicle_current_state_active_alerts ON vehicle_current_state(active_alerts_count) WHERE active_alerts_count > 0;

-- ========================================
-- TRIGGER TO AUTO-UPDATE vehicle_current_state
-- ========================================
-- Function to update current state timestamp
CREATE OR REPLACE FUNCTION update_vehicle_current_state_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update timestamp
CREATE TRIGGER trigger_update_vehicle_current_state_timestamp
    BEFORE UPDATE ON vehicle_current_state
    FOR EACH ROW
    EXECUTE FUNCTION update_vehicle_current_state_timestamp();

-- ========================================
-- COMMENTS FOR DOCUMENTATION
-- ========================================
COMMENT ON TABLE vehicle_events IS 'Historical record of all vehicle events for audit and analysis';
COMMENT ON TABLE vehicle_current_state IS 'Current snapshot state of each vehicle for quick access';

COMMENT ON COLUMN vehicle_events.event_type IS 'Type of event: TRIP_STARTED, TRIP_ENDED, CHARGING_STARTED, CHARGING_COMPLETED, MAINTENANCE_SCHEDULED, MAINTENANCE_COMPLETED, ALERT_RAISED, ALERT_RESOLVED, STATUS_CHANGED, etc.';
COMMENT ON COLUMN vehicle_events.severity IS 'Severity level: INFO, LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN vehicle_events.event_data IS 'JSON object containing event-specific data';
COMMENT ON COLUMN vehicle_events.source IS 'Source of the event: TELEMETRY, MANUAL, SYSTEM, EXTERNAL';

COMMENT ON COLUMN vehicle_current_state.status IS 'Current vehicle status: ACTIVE, IDLE, CHARGING, IN_MAINTENANCE, OFFLINE';
COMMENT ON COLUMN vehicle_current_state.efficiency_score IS 'Calculated efficiency score (0-100)';
