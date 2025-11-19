-- Migration: Create TCO analyses table
-- Version: V3
-- Description: Create table for storing Total Cost of Ownership analysis data

CREATE TABLE IF NOT EXISTS tco_analyses (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    analysis_date DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- Vehicle costs
    purchase_price NUMERIC(10, 2) NOT NULL DEFAULT 0,
    depreciation_value NUMERIC(10, 2) DEFAULT 0,
    
    -- Operating costs (lifetime or period-based)
    energy_costs NUMERIC(10, 2) DEFAULT 0,
    maintenance_costs NUMERIC(10, 2) DEFAULT 0,
    insurance_costs NUMERIC(10, 2) DEFAULT 0,
    taxes_fees NUMERIC(10, 2) DEFAULT 0,
    other_costs NUMERIC(10, 2) DEFAULT 0,
    
    -- Calculated totals
    total_cost NUMERIC(10, 2) NOT NULL DEFAULT 0,
    cost_per_km NUMERIC(10, 4) DEFAULT 0,
    cost_per_year NUMERIC(10, 2) DEFAULT 0,
    
    -- ICE comparison (for EVs)
    ice_fuel_savings NUMERIC(10, 2) DEFAULT 0,
    ice_maintenance_savings NUMERIC(10, 2) DEFAULT 0,
    ice_total_savings NUMERIC(10, 2) DEFAULT 0,
    ice_payback_period_months INTEGER DEFAULT 0,
    
    -- Metadata
    analysis_period_years INTEGER DEFAULT 5,
    total_distance_km NUMERIC(10, 2) DEFAULT 0,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Add indexes for performance
CREATE INDEX idx_tco_vehicle ON tco_analyses(vehicle_id);
CREATE INDEX idx_tco_company ON tco_analyses(company_id);
CREATE INDEX idx_tco_date ON tco_analyses(analysis_date);
CREATE INDEX idx_tco_vehicle_date ON tco_analyses(vehicle_id, analysis_date DESC);

-- Add comments
COMMENT ON TABLE tco_analyses IS 'Stores Total Cost of Ownership analysis for vehicles';
COMMENT ON COLUMN tco_analyses.purchase_price IS 'Vehicle purchase price';
COMMENT ON COLUMN tco_analyses.depreciation_value IS 'Depreciation amount';
COMMENT ON COLUMN tco_analyses.energy_costs IS 'Total energy/charging costs';
COMMENT ON COLUMN tco_analyses.maintenance_costs IS 'Total maintenance costs';
COMMENT ON COLUMN tco_analyses.total_cost IS 'Total cost of ownership';
COMMENT ON COLUMN tco_analyses.cost_per_km IS 'Cost per kilometer';
COMMENT ON COLUMN tco_analyses.ice_total_savings IS 'Total savings vs equivalent ICE vehicle';
COMMENT ON COLUMN tco_analyses.ice_payback_period_months IS 'Payback period in months vs ICE';
