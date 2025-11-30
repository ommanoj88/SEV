-- Flyway Migration: V1__create_telemetry_snapshots_table.sql
-- Creates the telemetry_snapshots table for storing historical telemetry data
-- Author: SEV Platform Team
-- Date: 2025-11-30

CREATE TABLE IF NOT EXISTS telemetry_snapshots (
    id BIGSERIAL PRIMARY KEY,
    
    -- Identification
    vehicle_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    device_id VARCHAR(50),
    telemetry_source VARCHAR(20),
    provider_name VARCHAR(50),
    
    -- Timestamp
    timestamp TIMESTAMP NOT NULL,
    data_quality VARCHAR(20),
    
    -- Location
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    altitude DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    satellites INTEGER,
    
    -- Odometer & Distance
    odometer DOUBLE PRECISION,
    trip_distance DOUBLE PRECISION,
    
    -- EV Battery Data
    battery_soc DOUBLE PRECISION,
    battery_soh DOUBLE PRECISION,
    battery_voltage DOUBLE PRECISION,
    battery_current DOUBLE PRECISION,
    battery_temperature DOUBLE PRECISION,
    estimated_range DOUBLE PRECISION,
    is_charging BOOLEAN,
    charging_status VARCHAR(20),
    
    -- ICE/Hybrid Fuel Data
    fuel_level DOUBLE PRECISION,
    fuel_percentage DOUBLE PRECISION,
    
    -- Vehicle Status
    ignition_on BOOLEAN,
    is_moving BOOLEAN,
    engine_rpm DOUBLE PRECISION,
    vehicle_status VARCHAR(20),
    
    -- Diagnostics
    check_engine_light BOOLEAN,
    dtc_count INTEGER,
    
    -- Driver Behavior (accelerometer)
    acceleration_x DOUBLE PRECISION,
    acceleration_y DOUBLE PRECISION,
    acceleration_z DOUBLE PRECISION,
    
    -- Metadata
    is_estimated BOOLEAN,
    signal_strength INTEGER,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_telemetry_vehicle FOREIGN KEY (vehicle_id) 
        REFERENCES vehicles(id) ON DELETE CASCADE,
    CONSTRAINT fk_telemetry_company FOREIGN KEY (company_id) 
        REFERENCES companies(id) ON DELETE CASCADE
);

-- Performance Indexes
-- Primary query pattern: Get history for a vehicle within a time range
CREATE INDEX idx_telemetry_vehicle_timestamp 
    ON telemetry_snapshots(vehicle_id, timestamp DESC);

-- Secondary: Company-wide queries
CREATE INDEX idx_telemetry_company_timestamp 
    ON telemetry_snapshots(company_id, timestamp DESC);

-- For retention policy cleanup
CREATE INDEX idx_telemetry_timestamp 
    ON telemetry_snapshots(timestamp);

-- For device-based lookups
CREATE INDEX idx_telemetry_device 
    ON telemetry_snapshots(device_id, timestamp DESC);

-- Partial index for charging analysis (only rows where charging)
CREATE INDEX idx_telemetry_charging 
    ON telemetry_snapshots(vehicle_id, timestamp) 
    WHERE is_charging = true;

-- Add comment for documentation
COMMENT ON TABLE telemetry_snapshots IS 
    'Historical telemetry data from all vehicle sources. 90-day retention policy.';

COMMENT ON COLUMN telemetry_snapshots.battery_soc IS 
    'State of Charge (0-100%). Only for EV/Hybrid vehicles.';

COMMENT ON COLUMN telemetry_snapshots.data_quality IS 
    'REAL_TIME, RECENT, or STALE based on data freshness.';
