-- V4__create_feature_toggles.sql
-- Migration to create feature toggle system
-- @since 2.0.0 (Feature flag system)

-- Create feature_toggles table
CREATE TABLE IF NOT EXISTS feature_toggles (
    id BIGSERIAL PRIMARY KEY,
    feature_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT idx_feature_name_unique UNIQUE (feature_name)
);

-- Create junction table for feature toggle fuel types
CREATE TABLE IF NOT EXISTS feature_toggle_fuel_types (
    feature_toggle_id BIGINT NOT NULL,
    fuel_type VARCHAR(20) NOT NULL,
    CONSTRAINT fk_feature_toggle 
        FOREIGN KEY (feature_toggle_id) 
        REFERENCES feature_toggles(id) 
        ON DELETE CASCADE,
    CONSTRAINT pk_feature_fuel_type 
        PRIMARY KEY (feature_toggle_id, fuel_type)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_feature_name ON feature_toggles(feature_name);
CREATE INDEX IF NOT EXISTS idx_feature_enabled ON feature_toggles(enabled);
CREATE INDEX IF NOT EXISTS idx_feature_fuel_type ON feature_toggle_fuel_types(fuel_type);

-- Pre-populate feature toggles with default features
-- Battery and charging features (EV and HYBRID only)
INSERT INTO feature_toggles (feature_name, description, enabled) VALUES
('BATTERY_TRACKING', 'Track battery state of charge, health, and charging status', true),
('CHARGING_MANAGEMENT', 'Manage charging sessions, schedule charging, and find charging stations', true),
('BATTERY_HEALTH', 'Monitor battery health metrics and degradation', true),
('CHARGING_STATION_DISCOVERY', 'Discover and navigate to nearby charging stations', true),
('REGENERATIVE_BRAKING', 'Track energy recovered through regenerative braking', true),
('ENERGY_OPTIMIZATION', 'Optimize energy consumption and route planning for electric vehicles', true);

-- Fuel consumption features (ICE and HYBRID only)
INSERT INTO feature_toggles (feature_name, description, enabled) VALUES
('FUEL_CONSUMPTION', 'Track fuel consumption, fuel level, and refueling history', true),
('ENGINE_DIAGNOSTICS', 'Monitor engine health, RPM, temperature, and other diagnostics', true),
('FUEL_STATION_DISCOVERY', 'Discover and navigate to nearby fuel stations', true);

-- Common features (available for all fuel types)
INSERT INTO feature_toggles (feature_name, description, enabled) VALUES
('RANGE_PREDICTION', 'Predict remaining range based on current consumption patterns', true),
('TRIP_COST_ANALYSIS', 'Calculate and analyze trip costs including energy/fuel and maintenance', true),
('CARBON_FOOTPRINT', 'Calculate and track carbon emissions for trips', true);

-- Insert fuel type associations for battery/charging features
INSERT INTO feature_toggle_fuel_types (feature_toggle_id, fuel_type)
SELECT id, 'EV' FROM feature_toggles WHERE feature_name IN (
    'BATTERY_TRACKING',
    'CHARGING_MANAGEMENT',
    'BATTERY_HEALTH',
    'CHARGING_STATION_DISCOVERY',
    'REGENERATIVE_BRAKING',
    'ENERGY_OPTIMIZATION'
);

INSERT INTO feature_toggle_fuel_types (feature_toggle_id, fuel_type)
SELECT id, 'HYBRID' FROM feature_toggles WHERE feature_name IN (
    'BATTERY_TRACKING',
    'CHARGING_MANAGEMENT',
    'BATTERY_HEALTH',
    'CHARGING_STATION_DISCOVERY',
    'REGENERATIVE_BRAKING',
    'ENERGY_OPTIMIZATION'
);

-- Insert fuel type associations for fuel/engine features
INSERT INTO feature_toggle_fuel_types (feature_toggle_id, fuel_type)
SELECT id, 'ICE' FROM feature_toggles WHERE feature_name IN (
    'FUEL_CONSUMPTION',
    'ENGINE_DIAGNOSTICS',
    'FUEL_STATION_DISCOVERY'
);

INSERT INTO feature_toggle_fuel_types (feature_toggle_id, fuel_type)
SELECT id, 'HYBRID' FROM feature_toggles WHERE feature_name IN (
    'FUEL_CONSUMPTION',
    'ENGINE_DIAGNOSTICS',
    'FUEL_STATION_DISCOVERY'
);

-- Insert fuel type associations for common features (all fuel types)
INSERT INTO feature_toggle_fuel_types (feature_toggle_id, fuel_type)
SELECT id, fuel_type FROM feature_toggles, (VALUES ('EV'), ('ICE'), ('HYBRID')) AS ft(fuel_type)
WHERE feature_name IN (
    'RANGE_PREDICTION',
    'TRIP_COST_ANALYSIS',
    'CARBON_FOOTPRINT'
);

-- Add comments for documentation
COMMENT ON TABLE feature_toggles IS 'Stores feature toggle configuration for conditional feature availability';
COMMENT ON TABLE feature_toggle_fuel_types IS 'Maps features to supported fuel types (EV, ICE, HYBRID)';
COMMENT ON COLUMN feature_toggles.feature_name IS 'Unique identifier for the feature';
COMMENT ON COLUMN feature_toggles.enabled IS 'Global enable/disable flag for the feature';
COMMENT ON COLUMN feature_toggle_fuel_types.fuel_type IS 'Fuel type that supports this feature (EV, ICE, or HYBRID)';
