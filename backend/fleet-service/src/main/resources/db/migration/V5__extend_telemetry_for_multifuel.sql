-- V5__extend_telemetry_for_multifuel.sql
-- PR #3: Extend Telemetry for Multi-Fuel
-- Adds ICE-specific telemetry fields to support ICE and HYBRID vehicles
--
-- This migration:
-- 1. Adds fuel_level column for tracking fuel tank levels
-- 2. Adds engine_rpm column for engine speed monitoring
-- 3. Adds engine_temperature column for engine health
-- 4. Adds engine_load column for engine performance tracking
-- 5. Adds engine_hours column for maintenance scheduling
--
-- Backward Compatibility:
-- - All new columns are nullable (optional)
-- - Existing telemetry data is unaffected
-- - EV-specific fields remain unchanged

-- Add ICE-specific telemetry columns (all nullable for backward compatibility)
ALTER TABLE telemetry_data 
ADD COLUMN fuel_level DOUBLE PRECISION,
ADD COLUMN engine_rpm INTEGER,
ADD COLUMN engine_temperature DOUBLE PRECISION,
ADD COLUMN engine_load DOUBLE PRECISION,
ADD COLUMN engine_hours DOUBLE PRECISION;

-- Create indexes for efficient querying of ICE metrics
-- These indexes help with queries filtering by ICE-specific metrics
CREATE INDEX IF NOT EXISTS idx_telemetry_fuel_level ON telemetry_data(vehicle_id, fuel_level) 
WHERE fuel_level IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_telemetry_engine_rpm ON telemetry_data(vehicle_id, engine_rpm) 
WHERE engine_rpm IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_telemetry_engine_temp ON telemetry_data(vehicle_id, engine_temperature) 
WHERE engine_temperature IS NOT NULL;

-- Composite index for engine diagnostics queries
CREATE INDEX IF NOT EXISTS idx_telemetry_engine_diagnostics 
ON telemetry_data(vehicle_id, timestamp DESC, engine_rpm, engine_temperature, engine_load)
WHERE engine_rpm IS NOT NULL;

-- Add check constraints for data integrity
ALTER TABLE telemetry_data 
ADD CONSTRAINT chk_fuel_level_range 
CHECK (fuel_level IS NULL OR fuel_level >= 0);

ALTER TABLE telemetry_data 
ADD CONSTRAINT chk_engine_rpm_range 
CHECK (engine_rpm IS NULL OR (engine_rpm >= 0 AND engine_rpm <= 10000));

ALTER TABLE telemetry_data 
ADD CONSTRAINT chk_engine_temp_range 
CHECK (engine_temperature IS NULL OR (engine_temperature >= -40 AND engine_temperature <= 200));

ALTER TABLE telemetry_data 
ADD CONSTRAINT chk_engine_load_range 
CHECK (engine_load IS NULL OR (engine_load >= 0 AND engine_load <= 100));

ALTER TABLE telemetry_data 
ADD CONSTRAINT chk_engine_hours_range 
CHECK (engine_hours IS NULL OR engine_hours >= 0);

-- Add comments for documentation
COMMENT ON COLUMN telemetry_data.fuel_level IS 'Current fuel level in liters (ICE and HYBRID vehicles)';
COMMENT ON COLUMN telemetry_data.engine_rpm IS 'Engine revolutions per minute (ICE and HYBRID vehicles)';
COMMENT ON COLUMN telemetry_data.engine_temperature IS 'Engine temperature in Celsius (ICE and HYBRID vehicles)';
COMMENT ON COLUMN telemetry_data.engine_load IS 'Engine load percentage 0-100 (ICE and HYBRID vehicles)';
COMMENT ON COLUMN telemetry_data.engine_hours IS 'Total engine operating hours for maintenance (ICE and HYBRID vehicles)';
