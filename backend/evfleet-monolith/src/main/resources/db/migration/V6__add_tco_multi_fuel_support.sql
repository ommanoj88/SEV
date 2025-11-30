-- V6: Add multi-fuel TCO analysis support
-- Adds columns for carbon tracking, regional adjustments, and 5-year projections

-- Add fuel type to TCO analysis
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS fuel_type VARCHAR(20);

-- Add current value tracking
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS current_value DECIMAL(10, 2) DEFAULT 0;

-- Add carbon emissions and costs
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS carbon_emissions_kg DECIMAL(12, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS carbon_cost DECIMAL(10, 2) DEFAULT 0;

-- Add regional adjustment fields
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS region_code VARCHAR(20);
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS regional_adjustment_factor DECIMAL(4, 2) DEFAULT 1.00;

-- Add multi-fuel comparison fields
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS comparison_fuel_type VARCHAR(20);
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS comparison_fuel_savings DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS comparison_maintenance_savings DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS comparison_carbon_savings DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS comparison_total_savings DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS comparison_payback_months INTEGER DEFAULT 0;

-- Add 5-year projection fields
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS projected_5yr_total_cost DECIMAL(12, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS projected_5yr_energy_cost DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS projected_5yr_maintenance_cost DECIMAL(10, 2) DEFAULT 0;
ALTER TABLE tco_analyses ADD COLUMN IF NOT EXISTS projected_5yr_carbon_cost DECIMAL(10, 2) DEFAULT 0;

-- Create index for fuel type analysis
CREATE INDEX IF NOT EXISTS idx_tco_analyses_fuel_type ON tco_analyses(fuel_type);
CREATE INDEX IF NOT EXISTS idx_tco_analyses_company_fuel ON tco_analyses(company_id, fuel_type);

-- Add comment for documentation
COMMENT ON COLUMN tco_analyses.fuel_type IS 'Vehicle fuel type: EV, ICE, HYBRID, CNG, LPG, DIESEL';
COMMENT ON COLUMN tco_analyses.carbon_emissions_kg IS 'Total carbon emissions in kg CO2';
COMMENT ON COLUMN tco_analyses.carbon_cost IS 'Carbon cost based on social cost of carbon (₹5/kg)';
COMMENT ON COLUMN tco_analyses.region_code IS 'City code for regional fuel price adjustments';
COMMENT ON COLUMN tco_analyses.projected_5yr_total_cost IS '5-year projected total cost with inflation';
