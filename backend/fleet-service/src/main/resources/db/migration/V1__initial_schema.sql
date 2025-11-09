-- V1__initial_schema.sql
-- Initial database schema for Fleet Service
-- Creates core tables for vehicles, telemetry, trips, and geofences

-- Create vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    vehicle_number VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    battery_capacity DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    current_battery_soc DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    last_updated TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vin VARCHAR(17),
    license_plate VARCHAR(20),
    color VARCHAR(50),
    current_driver_id BIGINT,
    total_distance DOUBLE PRECISION DEFAULT 0,
    total_energy_consumed DOUBLE PRECISION DEFAULT 0
);

-- Create indexes for vehicles
CREATE INDEX IF NOT EXISTS idx_company_id ON vehicles(company_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_number ON vehicles(vehicle_number);
CREATE INDEX IF NOT EXISTS idx_status ON vehicles(status);

-- Create telemetry_data table
CREATE TABLE IF NOT EXISTS telemetry_data (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    speed DOUBLE PRECISION,
    battery_soc DOUBLE PRECISION,
    battery_voltage DOUBLE PRECISION,
    battery_current DOUBLE PRECISION,
    battery_temperature DOUBLE PRECISION,
    odometer DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    altitude DOUBLE PRECISION,
    satellites INTEGER,
    signal_strength INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Create indexes for telemetry
CREATE INDEX IF NOT EXISTS idx_telemetry_vehicle_id ON telemetry_data(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_timestamp ON telemetry_data(timestamp);
CREATE INDEX IF NOT EXISTS idx_telemetry_vehicle_timestamp ON telemetry_data(vehicle_id, timestamp DESC);

-- Create trips table
CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    start_latitude DOUBLE PRECISION NOT NULL,
    start_longitude DOUBLE PRECISION NOT NULL,
    end_latitude DOUBLE PRECISION,
    end_longitude DOUBLE PRECISION,
    distance_km DOUBLE PRECISION DEFAULT 0,
    duration_minutes INTEGER,
    start_battery_soc DOUBLE PRECISION,
    end_battery_soc DOUBLE PRECISION,
    energy_consumed DOUBLE PRECISION,
    average_speed DOUBLE PRECISION,
    max_speed DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Create indexes for trips
CREATE INDEX IF NOT EXISTS idx_trips_vehicle_id ON trips(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_trips_driver_id ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_start_time ON trips(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);

-- Create geofences table
CREATE TABLE IF NOT EXISTS geofences (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    center_latitude DOUBLE PRECISION NOT NULL,
    center_longitude DOUBLE PRECISION NOT NULL,
    radius_meters DOUBLE PRECISION NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for geofences
CREATE INDEX IF NOT EXISTS idx_geofence_company_id ON geofences(company_id);
CREATE INDEX IF NOT EXISTS idx_geofence_is_active ON geofences(is_active);

-- Add comments for documentation
COMMENT ON TABLE vehicles IS 'Stores all vehicle information in the fleet';
COMMENT ON TABLE telemetry_data IS 'Real-time telemetry data from vehicles';
COMMENT ON TABLE trips IS 'Vehicle trip records with start/end information';
COMMENT ON TABLE geofences IS 'Geofence definitions for location-based alerts';
