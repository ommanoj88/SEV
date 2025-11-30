-- =====================================================
-- V7: Create ESG (Environmental, Social, Governance) Reports Table
-- =====================================================
-- Migration for PR #23: ESG Reporting Module
-- 
-- Stores sustainability metrics, carbon emissions tracking,
-- and compliance data for fleet environmental reporting
-- =====================================================

-- Create ESG Reports table
CREATE TABLE IF NOT EXISTS esg_reports (
    id BIGSERIAL PRIMARY KEY,
    
    -- Company reference
    company_id BIGINT NOT NULL,
    
    -- Report identification
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    compliance_standard VARCHAR(20),
    
    -- Reporting period
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Carbon Emissions (Scope 1, 2, 3) in kg CO2
    scope1_emissions_kg DECIMAL(15, 2),
    scope2_emissions_kg DECIMAL(15, 2),
    scope3_emissions_kg DECIMAL(15, 2),
    total_emissions_kg DECIMAL(15, 2),
    
    -- Carbon Savings vs ICE Baseline
    baseline_ice_emissions_kg DECIMAL(15, 2),
    carbon_savings_kg DECIMAL(15, 2),
    emissions_reduction_percent DECIMAL(5, 2),
    
    -- Fleet Metrics
    total_kilometers_driven DECIMAL(12, 2),
    total_energy_kwh DECIMAL(12, 2),
    total_fuel_liters DECIMAL(12, 2),
    
    -- Vehicle Counts
    ev_vehicle_count INTEGER,
    ice_vehicle_count INTEGER,
    hybrid_vehicle_count INTEGER,
    electrification_percent DECIMAL(5, 2),
    
    -- Cost Metrics (in Rupees)
    carbon_cost_rupees DECIMAL(12, 2),
    carbon_savings_rupees DECIMAL(12, 2),
    
    -- Intensity Metrics
    carbon_intensity_per_km DECIMAL(8, 4),
    energy_intensity_per_km DECIMAL(8, 4),
    fuel_intensity_per_km DECIMAL(8, 4),
    
    -- Compliance
    is_compliant BOOLEAN,
    compliance_notes VARCHAR(1000),
    
    -- Report Metadata
    generated_by BIGINT,
    generated_at TIMESTAMP,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT',
    
    -- Additional JSON data for extensibility
    additional_data TEXT,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX idx_esg_company_period ON esg_reports(company_id, period_start, period_end);
CREATE INDEX idx_esg_report_type ON esg_reports(report_type);
CREATE INDEX idx_esg_compliance_standard ON esg_reports(compliance_standard);
CREATE INDEX idx_esg_status ON esg_reports(status);
CREATE INDEX idx_esg_created_at ON esg_reports(created_at);
CREATE INDEX idx_esg_period_end ON esg_reports(period_end DESC);

-- Add comments for documentation
COMMENT ON TABLE esg_reports IS 'Environmental, Social, and Governance reports for fleet sustainability tracking';
COMMENT ON COLUMN esg_reports.scope1_emissions_kg IS 'Direct emissions from fleet vehicles (petrol, diesel, CNG, LPG) in kg CO2';
COMMENT ON COLUMN esg_reports.scope2_emissions_kg IS 'Indirect emissions from electricity consumption (EV charging) in kg CO2';
COMMENT ON COLUMN esg_reports.scope3_emissions_kg IS 'Other indirect emissions (maintenance, supply chain) in kg CO2';
COMMENT ON COLUMN esg_reports.baseline_ice_emissions_kg IS 'Emissions if entire fleet were ICE vehicles (for comparison)';
COMMENT ON COLUMN esg_reports.carbon_savings_kg IS 'CO2 savings vs ICE baseline';
COMMENT ON COLUMN esg_reports.electrification_percent IS 'Percentage of fleet that is EV (EVs = 100%, Hybrids = 50%)';
COMMENT ON COLUMN esg_reports.carbon_intensity_per_km IS 'Average kg CO2 emitted per km driven';
COMMENT ON COLUMN esg_reports.compliance_standard IS 'SEBI_BRSR, GRI, CDP, TCFD, ISO_14064, or CUSTOM';
COMMENT ON COLUMN esg_reports.report_type IS 'MONTHLY, QUARTERLY, ANNUAL, or CUSTOM';
COMMENT ON COLUMN esg_reports.status IS 'DRAFT, PENDING_REVIEW, APPROVED, PUBLISHED, or ARCHIVED';

-- Constraint for valid report types
ALTER TABLE esg_reports ADD CONSTRAINT chk_esg_report_type 
    CHECK (report_type IN ('MONTHLY', 'QUARTERLY', 'ANNUAL', 'CUSTOM'));

-- Constraint for valid compliance standards
ALTER TABLE esg_reports ADD CONSTRAINT chk_esg_compliance_standard 
    CHECK (compliance_standard IS NULL OR compliance_standard IN ('SEBI_BRSR', 'GRI', 'CDP', 'TCFD', 'ISO_14064', 'CUSTOM'));

-- Constraint for valid status
ALTER TABLE esg_reports ADD CONSTRAINT chk_esg_status 
    CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED'));

-- Constraint for valid period
ALTER TABLE esg_reports ADD CONSTRAINT chk_esg_period 
    CHECK (period_end >= period_start);

-- Constraint for non-negative emissions
ALTER TABLE esg_reports ADD CONSTRAINT chk_esg_emissions_positive 
    CHECK (
        (scope1_emissions_kg IS NULL OR scope1_emissions_kg >= 0) AND
        (scope2_emissions_kg IS NULL OR scope2_emissions_kg >= 0) AND
        (scope3_emissions_kg IS NULL OR scope3_emissions_kg >= 0)
    );

-- Constraint for valid percentages
ALTER TABLE esg_reports ADD CONSTRAINT chk_esg_percentages 
    CHECK (
        (emissions_reduction_percent IS NULL OR emissions_reduction_percent BETWEEN -100 AND 100) AND
        (electrification_percent IS NULL OR electrification_percent BETWEEN 0 AND 100)
    );
