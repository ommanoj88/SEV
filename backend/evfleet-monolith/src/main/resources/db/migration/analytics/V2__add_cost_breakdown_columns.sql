-- Migration: Add cost breakdown columns to fleet_summaries
-- Version: V2
-- Description: Add maintenance_cost, fuel_cost, and energy_cost columns for detailed cost tracking

ALTER TABLE fleet_summaries 
    ADD COLUMN IF NOT EXISTS maintenance_cost NUMERIC(10, 2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS fuel_cost NUMERIC(10, 2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS energy_cost NUMERIC(10, 2) DEFAULT 0.00;

-- Add comments
COMMENT ON COLUMN fleet_summaries.maintenance_cost IS 'Total maintenance cost for the day';
COMMENT ON COLUMN fleet_summaries.fuel_cost IS 'Total fuel cost for ICE vehicles for the day';
COMMENT ON COLUMN fleet_summaries.energy_cost IS 'Total energy/charging cost for EV vehicles for the day';

-- Update existing records to initialize the new columns
UPDATE fleet_summaries 
SET maintenance_cost = 0.00, 
    fuel_cost = 0.00, 
    energy_cost = 0.00 
WHERE maintenance_cost IS NULL 
   OR fuel_cost IS NULL 
   OR energy_cost IS NULL;
