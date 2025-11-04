-- Flyway Migration V1: Create Analytics Service Tables with TimescaleDB

-- Enable TimescaleDB extension (requires TimescaleDB to be installed)
-- CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Cost Analytics Table (Time-Series Data)
CREATE TABLE IF NOT EXISTS cost_analytics (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    vehicle_id VARCHAR(255),
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    energy_cost DECIMAL(12, 2) DEFAULT 0,
    maintenance_cost DECIMAL(12, 2) DEFAULT 0,
    total_cost DECIMAL(12, 2) DEFAULT 0,
    cost_per_km DECIMAL(10, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Convert to hypertable for time-series optimization (TimescaleDB)
-- SELECT create_hypertable('cost_analytics', 'period_start', if_not_exists => TRUE);

CREATE INDEX idx_cost_company ON cost_analytics (company_id);
CREATE INDEX idx_cost_vehicle ON cost_analytics (vehicle_id);
CREATE INDEX idx_cost_period ON cost_analytics (period_start, period_end);

-- Utilization Report Table
CREATE TABLE IF NOT EXISTS utilization_reports (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    active_hours DECIMAL(10, 2) DEFAULT 0,
    idle_hours DECIMAL(10, 2) DEFAULT 0,
    charging_hours DECIMAL(10, 2) DEFAULT 0,
    utilization_percentage DECIMAL(5, 2),
    distance_traveled DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_util_vehicle ON utilization_reports (vehicle_id);
CREATE INDEX idx_util_period ON utilization_reports (period_start);

-- Carbon Footprint Table
CREATE TABLE IF NOT EXISTS carbon_footprint (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    co2_saved_kg DECIMAL(12, 2),
    emissions_avoided_kg DECIMAL(12, 2),
    trees_equivalent INTEGER,
    methodology VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_carbon_company ON carbon_footprint (company_id);
CREATE INDEX idx_carbon_period ON carbon_footprint (period_start);

-- Custom Reports Table
CREATE TABLE IF NOT EXISTS custom_reports (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    filters JSONB,
    schedule VARCHAR(50), -- DAILY, WEEKLY, MONTHLY
    last_generated TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_company ON custom_reports (company_id);
CREATE INDEX idx_reports_type ON custom_reports (report_type);

-- Fleet Summary Materialized View
CREATE MATERIALIZED VIEW IF NOT EXISTS fleet_summary AS
SELECT
    company_id,
    COUNT(DISTINCT vehicle_id) as total_vehicles,
    AVG(utilization_percentage) as avg_utilization,
    SUM(distance_traveled) as total_distance,
    MAX(period_end) as last_updated
FROM utilization_reports
GROUP BY company_id;

CREATE INDEX idx_fleet_summary_company ON fleet_summary (company_id);

-- TCO (Total Cost of Ownership) Analysis View
CREATE MATERIALIZED VIEW IF NOT EXISTS tco_analysis AS
SELECT
    vehicle_id,
    SUM(energy_cost) as total_energy_cost,
    SUM(maintenance_cost) as total_maintenance_cost,
    SUM(total_cost) as total_ownership_cost,
    AVG(cost_per_km) as avg_cost_per_km,
    COUNT(*) as analysis_periods
FROM cost_analytics
WHERE period_start >= CURRENT_DATE - INTERVAL '365 days'
GROUP BY vehicle_id;

CREATE INDEX idx_tco_vehicle ON tco_analysis (vehicle_id);

-- Energy Consumption Trends Table
CREATE TABLE IF NOT EXISTS energy_consumption_trends (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    energy_kwh DECIMAL(10, 3),
    efficiency_kwh_per_km DECIMAL(8, 4),
    temperature_celsius DECIMAL(5, 2),
    distance_km DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_energy_vehicle ON energy_consumption_trends (vehicle_id);
CREATE INDEX idx_energy_timestamp ON energy_consumption_trends (timestamp);

-- Performance Metrics Table
CREATE TABLE IF NOT EXISTS performance_metrics (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    metric_type VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15, 4),
    metric_unit VARCHAR(50),
    timestamp TIMESTAMP NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_metrics_company ON performance_metrics (company_id);
CREATE INDEX idx_metrics_type ON performance_metrics (metric_type);
CREATE INDEX idx_metrics_timestamp ON performance_metrics (timestamp);

-- Insert Sample Analytics Data
INSERT INTO cost_analytics (id, company_id, vehicle_id, period_start, period_end, energy_cost, maintenance_cost, total_cost, cost_per_km)
VALUES
    ('CA001', 'COMP001', 'VEH001', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 1500.00, 500.00, 2000.00, 8.50),
    ('CA002', 'COMP001', 'VEH002', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 1800.00, 300.00, 2100.00, 7.20),
    ('CA003', 'COMP001', 'VEH003', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 1200.00, 800.00, 2000.00, 9.10)
ON CONFLICT (id) DO NOTHING;

INSERT INTO utilization_reports (id, vehicle_id, period_start, period_end, active_hours, idle_hours, charging_hours, utilization_percentage, distance_traveled)
VALUES
    ('UR001', 'VEH001', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE, 120.5, 25.3, 22.2, 72.5, 850.5),
    ('UR002', 'VEH002', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE, 135.0, 18.5, 14.5, 80.5, 920.3),
    ('UR003', 'VEH003', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE, 105.2, 35.8, 27.0, 62.8, 680.2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO carbon_footprint (id, company_id, period_start, period_end, co2_saved_kg, emissions_avoided_kg, trees_equivalent)
VALUES
    ('CF001', 'COMP001', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 1250.50, 1500.75, 55),
    ('CF002', 'COMP002', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 980.25, 1180.30, 43)
ON CONFLICT (id) DO NOTHING;

-- Refresh materialized views
REFRESH MATERIALIZED VIEW fleet_summary;
REFRESH MATERIALIZED VIEW tco_analysis;

-- Function to refresh materialized views periodically
CREATE OR REPLACE FUNCTION refresh_analytics_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY fleet_summary;
    REFRESH MATERIALIZED VIEW CONCURRENTLY tco_analysis;
END;
$$ LANGUAGE plpgsql;

-- Update Timestamp Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_custom_reports_updated_at BEFORE UPDATE ON custom_reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
