-- Migration: Create energy consumption analytics table
-- Version: V4
-- Description: Create table for tracking detailed energy consumption analytics per vehicle

CREATE TABLE IF NOT EXISTS energy_consumption_analytics (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    analysis_date DATE NOT NULL,
    
    -- Energy metrics
    total_energy_consumed NUMERIC(10, 2) DEFAULT 0,  -- kWh
    total_distance NUMERIC(10, 2) DEFAULT 0,          -- km
    total_charging_sessions INTEGER DEFAULT 0,
    
    -- Efficiency metrics
    average_efficiency NUMERIC(10, 4) DEFAULT 0,     -- kWh per 100km
    best_efficiency NUMERIC(10, 4) DEFAULT 0,
    worst_efficiency NUMERIC(10, 4) DEFAULT 0,
    
    -- Cost metrics
    total_charging_cost NUMERIC(10, 2) DEFAULT 0,
    average_cost_per_kwh NUMERIC(10, 4) DEFAULT 0,
    cost_per_km NUMERIC(10, 4) DEFAULT 0,
    
    -- Advanced metrics (optional)
    regenerative_energy NUMERIC(10, 2) DEFAULT 0,    -- kWh recovered
    regen_percentage NUMERIC(10, 2) DEFAULT 0,       -- % of total energy
    idle_energy_loss NUMERIC(10, 2) DEFAULT 0,       -- kWh lost while parked
    
    -- Environmental metrics
    co2_saved NUMERIC(10, 2) DEFAULT 0,              -- kg CO2 vs equivalent ICE
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint
ALTER TABLE energy_consumption_analytics 
ADD CONSTRAINT uk_energy_vehicle_date UNIQUE(vehicle_id, analysis_date);

-- Add indexes
CREATE INDEX idx_energy_vehicle ON energy_consumption_analytics(vehicle_id);
CREATE INDEX idx_energy_company ON energy_consumption_analytics(company_id);
CREATE INDEX idx_energy_date ON energy_consumption_analytics(analysis_date);
CREATE INDEX idx_energy_vehicle_date ON energy_consumption_analytics(vehicle_id, analysis_date DESC);

-- Add comments
COMMENT ON TABLE energy_consumption_analytics IS 'Daily energy consumption analytics per vehicle';
COMMENT ON COLUMN energy_consumption_analytics.total_energy_consumed IS 'Total energy consumed in kWh';
COMMENT ON COLUMN energy_consumption_analytics.average_efficiency IS 'Average efficiency in kWh per 100km';
COMMENT ON COLUMN energy_consumption_analytics.regenerative_energy IS 'Energy recovered through regenerative braking';
COMMENT ON COLUMN energy_consumption_analytics.co2_saved IS 'CO2 emissions saved vs equivalent ICE vehicle';
