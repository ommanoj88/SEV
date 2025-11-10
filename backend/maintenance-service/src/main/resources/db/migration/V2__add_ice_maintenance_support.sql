-- Flyway Migration V2: Add Support for ICE Maintenance Types and Multi-Fuel Maintenance
-- PR 11: ICE Maintenance Services

-- This migration extends the maintenance service to support ICE-specific maintenance
-- It doesn't change the schema but adds reference data for ICE maintenance types

-- Add maintenance_type_category enum if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'maintenance_category') THEN
        CREATE TYPE maintenance_category AS ENUM ('ICE', 'EV', 'COMMON');
    END IF;
END $$;

-- Create a reference table for maintenance types (optional, for documentation)
CREATE TABLE IF NOT EXISTS maintenance_type_reference (
    maintenance_type VARCHAR(100) PRIMARY KEY,
    display_name VARCHAR(200) NOT NULL,
    category maintenance_category NOT NULL,
    default_mileage_interval INTEGER DEFAULT 0,
    default_time_interval_months INTEGER DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert ICE-specific maintenance types
INSERT INTO maintenance_type_reference (maintenance_type, display_name, category, default_mileage_interval, default_time_interval_months, description)
VALUES
    ('OIL_CHANGE', 'Oil Change', 'ICE', 5000, 6, 'Engine oil change service - Every 5,000-10,000 km or 6 months'),
    ('OIL_FILTER', 'Oil Filter Replacement', 'ICE', 5000, 6, 'Engine oil filter replacement - Every 5,000-10,000 km or 6 months'),
    ('AIR_FILTER', 'Air Filter Replacement', 'ICE', 15000, 12, 'Engine air filter replacement - Every 15,000-30,000 km or 12 months'),
    ('FUEL_FILTER', 'Fuel Filter Replacement', 'ICE', 20000, 24, 'Fuel filter replacement - Every 20,000-40,000 km or 24 months'),
    ('TRANSMISSION_FLUID', 'Transmission Fluid Change', 'ICE', 40000, 36, 'Transmission fluid change - Every 40,000-60,000 km or 36 months'),
    ('COOLANT_FLUSH', 'Coolant Flush', 'ICE', 40000, 24, 'Coolant/antifreeze flush - Every 40,000-60,000 km or 24 months'),
    ('SPARK_PLUGS', 'Spark Plug Replacement', 'ICE', 30000, 36, 'Spark plug replacement - Every 30,000-50,000 km or 36 months'),
    ('TIMING_BELT', 'Timing Belt Replacement', 'ICE', 60000, 60, 'Timing belt replacement - Every 60,000-100,000 km or 60 months'),
    ('ENGINE_TUNE_UP', 'Engine Tune-Up', 'ICE', 20000, 24, 'Engine tune-up - Every 20,000-30,000 km or 24 months'),
    ('EXHAUST_SYSTEM', 'Exhaust System Service', 'ICE', 15000, 12, 'Exhaust system inspection and repair - Every 15,000-20,000 km or 12 months'),
    ('FUEL_INJECTOR_CLEANING', 'Fuel Injector Cleaning', 'ICE', 25000, 24, 'Fuel injector cleaning - Every 25,000-30,000 km or 24 months')
ON CONFLICT (maintenance_type) DO NOTHING;

-- Insert EV-specific maintenance types
INSERT INTO maintenance_type_reference (maintenance_type, display_name, category, default_mileage_interval, default_time_interval_months, description)
VALUES
    ('BATTERY_CHECK', 'Battery Health Check', 'EV', 10000, 6, 'Battery health check and diagnostics - Every 10,000 km or 6 months'),
    ('BATTERY_MAINTENANCE', 'Battery Pack Maintenance', 'EV', 20000, 12, 'Battery pack inspection and maintenance - Every 20,000 km or 12 months'),
    ('BATTERY_COOLING', 'Battery Cooling System Check', 'EV', 15000, 12, 'Battery cooling system check - Every 15,000 km or 12 months'),
    ('MOTOR_INSPECTION', 'Electric Motor Inspection', 'EV', 20000, 12, 'Electric motor inspection - Every 20,000 km or 12 months'),
    ('HIGH_VOLTAGE_CHECK', 'High Voltage System Check', 'EV', 15000, 12, 'High voltage system check - Every 15,000 km or 12 months'),
    ('CHARGING_PORT_CHECK', 'Charging Port Inspection', 'EV', 10000, 6, 'Charging port and cable inspection - Every 10,000 km or 6 months'),
    ('POWER_ELECTRONICS_COOLING', 'Power Electronics Cooling', 'EV', 20000, 12, 'Power electronics cooling system - Every 20,000 km or 12 months')
ON CONFLICT (maintenance_type) DO NOTHING;

-- Insert common maintenance types (applicable to all vehicles)
INSERT INTO maintenance_type_reference (maintenance_type, display_name, category, default_mileage_interval, default_time_interval_months, description)
VALUES
    ('TIRE_ROTATION', 'Tire Rotation', 'COMMON', 8000, 6, 'Tire rotation and balancing - Every 8,000-10,000 km or 6 months'),
    ('TIRE_REPLACEMENT', 'Tire Replacement', 'COMMON', 40000, 36, 'Tire replacement - Every 40,000-50,000 km or as needed'),
    ('BRAKE_PADS', 'Brake Pad Service', 'COMMON', 15000, 12, 'Brake pad inspection and replacement - Every 15,000-20,000 km or 12 months'),
    ('BRAKE_FLUID', 'Brake Fluid Replacement', 'COMMON', 20000, 24, 'Brake fluid replacement - Every 20,000-30,000 km or 24 months'),
    ('BRAKE_INSPECTION', 'Brake System Inspection', 'COMMON', 10000, 6, 'Brake system inspection - Every 10,000 km or 6 months'),
    ('SUSPENSION_CHECK', 'Suspension System Check', 'COMMON', 15000, 12, 'Suspension system check - Every 15,000-20,000 km or 12 months'),
    ('WHEEL_ALIGNMENT', 'Wheel Alignment', 'COMMON', 15000, 12, 'Wheel alignment - Every 15,000-20,000 km or 12 months'),
    ('STEERING_CHECK', 'Steering System Check', 'COMMON', 15000, 12, 'Steering system inspection - Every 15,000 km or 12 months'),
    ('HVAC_SERVICE', 'HVAC System Service', 'COMMON', 15000, 12, 'HVAC system service - Every 15,000 km or 12 months'),
    ('CABIN_FILTER', 'Cabin Air Filter Replacement', 'COMMON', 15000, 12, 'Cabin air filter replacement - Every 15,000 km or 12 months'),
    ('WIPER_REPLACEMENT', 'Wiper Replacement', 'COMMON', 0, 6, 'Windshield wiper replacement - Every 6-12 months or as needed'),
    ('LIGHTING_CHECK', 'Lighting System Check', 'COMMON', 10000, 6, 'Lighting system check - Every 10,000 km or 6 months'),
    ('SAFETY_INSPECTION', 'General Safety Inspection', 'COMMON', 10000, 6, 'General safety inspection - Every 10,000 km or 6 months'),
    ('SOFTWARE_UPDATE', 'Software Update', 'COMMON', 0, 12, 'Software update - As available/needed')
ON CONFLICT (maintenance_type) DO NOTHING;

-- Create index on category for faster lookups by fuel type
CREATE INDEX IF NOT EXISTS idx_maintenance_type_category ON maintenance_type_reference (category);

-- Add comment to maintenance_schedules table to clarify service_type usage
COMMENT ON COLUMN maintenance_schedules.service_type IS 'Maintenance type - should match values from MaintenanceType enum (e.g., OIL_CHANGE, BATTERY_CHECK, TIRE_ROTATION)';

-- Update existing maintenance schedules to use new standardized types (if any exist)
-- This is safe to run even if there are no records
UPDATE maintenance_schedules 
SET service_type = 'BATTERY_CHECK'
WHERE service_type = 'Battery Check';

-- Add sample ICE maintenance schedules for demonstration (optional)
-- These will be generated programmatically by MaintenanceScheduleBuilder in production
INSERT INTO maintenance_schedules (id, vehicle_id, service_type, due_date, due_mileage, status, priority, description)
VALUES
    ('MAINT_ICE_001', 'VEH001', 'OIL_CHANGE', CURRENT_DATE + INTERVAL '180 days', 5000, 'SCHEDULED', 'HIGH', 'Engine oil change - Every 5,000 km or 6 months'),
    ('MAINT_ICE_002', 'VEH001', 'AIR_FILTER', CURRENT_DATE + INTERVAL '365 days', 15000, 'SCHEDULED', 'MEDIUM', 'Air filter replacement - Every 15,000 km or 12 months'),
    ('MAINT_ICE_003', 'VEH001', 'FUEL_FILTER', CURRENT_DATE + INTERVAL '730 days', 20000, 'SCHEDULED', 'MEDIUM', 'Fuel filter replacement - Every 20,000 km or 24 months')
ON CONFLICT (id) DO NOTHING;

-- Create a view to show maintenance schedules with their category
CREATE OR REPLACE VIEW maintenance_schedules_with_category AS
SELECT 
    ms.id,
    ms.vehicle_id,
    ms.service_type,
    mtr.display_name,
    mtr.category,
    ms.due_date,
    ms.due_mileage,
    ms.status,
    ms.priority,
    ms.description,
    ms.created_at,
    ms.updated_at
FROM maintenance_schedules ms
LEFT JOIN maintenance_type_reference mtr ON ms.service_type = mtr.maintenance_type;

-- Grant permissions on the new table and view
-- GRANT SELECT ON maintenance_type_reference TO maintenance_service_user;
-- GRANT SELECT ON maintenance_schedules_with_category TO maintenance_service_user;

COMMENT ON TABLE maintenance_type_reference IS 'Reference data for maintenance types supporting ICE, EV, and HYBRID vehicles';
COMMENT ON VIEW maintenance_schedules_with_category IS 'View showing maintenance schedules with their category information';
