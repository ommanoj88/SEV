-- V2__add_fuel_type_support.sql
-- PR #1: Add Vehicle Fuel Type Support
-- Adds support for ICE, EV, and HYBRID vehicles
--
-- This migration:
-- 1. Adds fuel_type column to vehicles table
-- 2. Adds ICE-specific fields (fuel_tank_capacity, fuel_level, engine_type)
-- 3. Adds default_charger_type for EV vehicles
-- 4. Adds total_fuel_consumed tracking
-- 5. Makes battery_capacity nullable for ICE vehicles
-- 6. Sets default fuel_type to 'EV' for backward compatibility
--
-- Backward Compatibility:
-- - All existing vehicles will default to 'EV' type
-- - battery_capacity remains required for existing EV vehicles
-- - New ICE vehicles won't require battery_capacity

-- Add fuel_type column with default value 'EV' for existing vehicles
ALTER TABLE vehicles 
ADD COLUMN fuel_type VARCHAR(20) DEFAULT 'EV';

-- Update existing NULL values to 'EV' (for safety)
UPDATE vehicles SET fuel_type = 'EV' WHERE fuel_type IS NULL;

-- Add ICE-specific columns (all nullable for backward compatibility)
ALTER TABLE vehicles 
ADD COLUMN fuel_tank_capacity DOUBLE PRECISION,
ADD COLUMN fuel_level DOUBLE PRECISION,
ADD COLUMN engine_type VARCHAR(50),
ADD COLUMN default_charger_type VARCHAR(50),
ADD COLUMN total_fuel_consumed DOUBLE PRECISION DEFAULT 0;

-- Make battery_capacity nullable (it's required for EV/HYBRID, optional for ICE)
-- Note: This depends on how the table was initially created
-- If battery_capacity was NOT NULL, we need to alter it
ALTER TABLE vehicles ALTER COLUMN battery_capacity DROP NOT NULL;

-- Create index on fuel_type for efficient filtering
CREATE INDEX IF NOT EXISTS idx_fuel_type ON vehicles(fuel_type);

-- Add check constraint to ensure valid fuel types
ALTER TABLE vehicles 
ADD CONSTRAINT chk_fuel_type CHECK (fuel_type IN ('ICE', 'EV', 'HYBRID'));

-- Add check constraint to ensure ICE/HYBRID vehicles have fuel tank capacity
-- Note: This is a soft constraint - we'll enforce it in application layer
-- ALTER TABLE vehicles ADD CONSTRAINT chk_ice_fuel_fields 
-- CHECK (
--     (fuel_type = 'ICE' AND fuel_tank_capacity IS NOT NULL) OR
--     (fuel_type = 'HYBRID' AND fuel_tank_capacity IS NOT NULL AND battery_capacity IS NOT NULL) OR
--     (fuel_type = 'EV' AND battery_capacity IS NOT NULL)
-- );

-- Add comments for new columns
COMMENT ON COLUMN vehicles.fuel_type IS 'Type of vehicle power source: ICE, EV, or HYBRID';
COMMENT ON COLUMN vehicles.fuel_tank_capacity IS 'Fuel tank capacity in liters (for ICE and HYBRID)';
COMMENT ON COLUMN vehicles.fuel_level IS 'Current fuel level in liters (for ICE and HYBRID)';
COMMENT ON COLUMN vehicles.engine_type IS 'Type of fuel: Petrol, Diesel, CNG, etc (for ICE and HYBRID)';
COMMENT ON COLUMN vehicles.default_charger_type IS 'Default charger type: CCS, CHAdeMO, Type 2, etc (for EV and HYBRID)';
COMMENT ON COLUMN vehicles.total_fuel_consumed IS 'Total fuel consumed by vehicle in liters (for ICE and HYBRID)';
