-- Historical Metrics Table for Analytics Aggregation
-- V5: Create historical_metrics table for trend analysis and data retention

CREATE TABLE IF NOT EXISTS historical_metrics (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_value DECIMAL(15, 4) NOT NULL,
    previous_value DECIMAL(15, 4),
    change_percent DECIMAL(8, 2),
    trend_direction VARCHAR(10),
    sample_count INTEGER,
    min_value DECIMAL(15, 4),
    max_value DECIMAL(15, 4),
    avg_value DECIMAL(15, 4),
    std_deviation DECIMAL(15, 4),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_period_type CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    CONSTRAINT chk_trend_direction CHECK (trend_direction IN ('UP', 'DOWN', 'STABLE')),
    CONSTRAINT chk_period_dates CHECK (period_end >= period_start),
    
    -- Unique constraint to prevent duplicate metrics
    CONSTRAINT uk_historical_metric UNIQUE (company_id, period_type, period_start, metric_type)
);

-- Performance Indexes
CREATE INDEX idx_hist_company_period ON historical_metrics(company_id, period_type, period_start);
CREATE INDEX idx_hist_metric_type ON historical_metrics(metric_type, company_id);
CREATE INDEX idx_hist_period_start ON historical_metrics(period_start);
CREATE INDEX idx_hist_created_at ON historical_metrics(created_at);
CREATE INDEX idx_hist_trend ON historical_metrics(company_id, trend_direction, period_start);

-- Partial index for recent data (frequently queried)
CREATE INDEX idx_hist_recent ON historical_metrics(company_id, period_type, period_start)
    WHERE period_start >= CURRENT_DATE - INTERVAL '90 days';

COMMENT ON TABLE historical_metrics IS 'Aggregated historical metrics for trend analysis and reporting';
COMMENT ON COLUMN historical_metrics.period_type IS 'Aggregation period: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY';
COMMENT ON COLUMN historical_metrics.metric_type IS 'Type of metric being tracked (e.g., TOTAL_VEHICLES, TOTAL_COST)';
COMMENT ON COLUMN historical_metrics.change_percent IS 'Percentage change from previous period';
COMMENT ON COLUMN historical_metrics.trend_direction IS 'Trend direction: UP, DOWN, or STABLE';
COMMENT ON COLUMN historical_metrics.sample_count IS 'Number of samples used in aggregation';
