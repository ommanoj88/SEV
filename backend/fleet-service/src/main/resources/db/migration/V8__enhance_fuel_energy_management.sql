-- Enhanced Fuel/Energy Management

-- Add new columns to fuel_consumption table for comprehensive tracking
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS fuel_efficiency DECIMAL(10, 2); -- km per liter or kWh per 100km
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS fuel_cost DECIMAL(10, 2);
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS fuel_vendor VARCHAR(255);
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS fuel_card_number VARCHAR(50);
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(100);
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS receipt_file_path VARCHAR(500);
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS odometer_before INT;
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS odometer_after INT;
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS driver_id BIGINT;
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS trip_id BIGINT;
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS fuel_theft_suspected BOOLEAN DEFAULT FALSE;
ALTER TABLE fuel_consumption ADD COLUMN IF NOT EXISTS theft_detection_reason TEXT;

-- Fuel Price History Table
CREATE TABLE IF NOT EXISTS fuel_prices (
    id BIGSERIAL PRIMARY KEY,
    fuel_type VARCHAR(50) NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    location VARCHAR(255),
    vendor VARCHAR(255),
    effective_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_fuel_price_positive CHECK (price_per_unit > 0)
);

-- Energy Consumption Analytics Table
CREATE TABLE IF NOT EXISTS energy_analytics (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    
    -- Period
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Consumption Metrics
    total_energy_consumed DECIMAL(10, 2), -- liters or kWh
    total_distance_traveled DECIMAL(10, 2), -- km
    average_efficiency DECIMAL(10, 2), -- km/l or kWh/100km
    
    -- Cost Metrics
    total_cost DECIMAL(12, 2),
    cost_per_km DECIMAL(10, 2),
    cost_per_unit DECIMAL(10, 2),
    
    -- Comparative Metrics
    efficiency_vs_baseline DECIMAL(5, 2), -- percentage
    cost_vs_budget DECIMAL(5, 2), -- percentage
    
    -- Breakdown by fuel type (for multi-fuel vehicles)
    petrol_consumed DECIMAL(10, 2),
    diesel_consumed DECIMAL(10, 2),
    cng_consumed DECIMAL(10, 2),
    electric_consumed DECIMAL(10, 2), -- kWh
    
    -- Analysis
    top_efficiency_routes TEXT[],
    low_efficiency_routes TEXT[],
    recommendations TEXT,
    
    -- Audit
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    
    CONSTRAINT chk_period_dates CHECK (period_end >= period_start)
);

-- Fuel Theft Detection Rules Table
CREATE TABLE IF NOT EXISTS fuel_theft_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL,
    rule_description TEXT,
    
    -- Threshold Parameters
    max_consumption_variance_percentage DECIMAL(5, 2), -- e.g., 20%
    min_refill_interval_hours INT, -- Minimum time between refills
    max_refill_amount DECIMAL(10, 2), -- Maximum single refill amount
    sudden_drop_threshold DECIMAL(10, 2), -- Sudden fuel level drop
    
    -- Detection Logic
    is_active BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 1, -- 1=High, 2=Medium, 3=Low
    
    -- Actions
    auto_flag_expense BOOLEAN DEFAULT TRUE,
    send_alert BOOLEAN DEFAULT TRUE,
    alert_recipients TEXT[],
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Fuel Card Management Table
CREATE TABLE IF NOT EXISTS fuel_cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(50) UNIQUE NOT NULL,
    card_provider VARCHAR(100) NOT NULL,
    
    -- Assignment
    assigned_to_entity_type VARCHAR(50), -- 'VEHICLE', 'DRIVER'
    assigned_to_entity_id BIGINT,
    vehicle_id BIGINT,
    driver_id BIGINT,
    
    -- Card Details
    card_holder_name VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    
    -- Limits
    daily_limit DECIMAL(10, 2),
    monthly_limit DECIMAL(10, 2),
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_blocked BOOLEAN DEFAULT FALSE,
    block_reason TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_fuel_consumption_driver ON fuel_consumption(driver_id);
CREATE INDEX IF NOT EXISTS idx_fuel_consumption_trip ON fuel_consumption(trip_id);
CREATE INDEX IF NOT EXISTS idx_fuel_consumption_theft ON fuel_consumption(fuel_theft_suspected);

CREATE INDEX idx_fuel_prices_fuel_type ON fuel_prices(fuel_type);
CREATE INDEX idx_fuel_prices_date ON fuel_prices(effective_date);
CREATE INDEX idx_fuel_prices_location ON fuel_prices(location);

CREATE INDEX idx_energy_analytics_vehicle ON energy_analytics(vehicle_id);
CREATE INDEX idx_energy_analytics_period ON energy_analytics(period_start, period_end);

CREATE INDEX idx_fuel_cards_card_number ON fuel_cards(card_number);
CREATE INDEX idx_fuel_cards_vehicle ON fuel_cards(vehicle_id);
CREATE INDEX idx_fuel_cards_driver ON fuel_cards(driver_id);
CREATE INDEX idx_fuel_cards_active ON fuel_cards(is_active);

-- Function to calculate fuel efficiency
CREATE OR REPLACE FUNCTION calculate_fuel_efficiency()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.odometer_after IS NOT NULL AND NEW.odometer_before IS NOT NULL AND NEW.amount IS NOT NULL THEN
        IF NEW.odometer_after > NEW.odometer_before AND NEW.amount > 0 THEN
            NEW.fuel_efficiency = (NEW.odometer_after - NEW.odometer_before)::DECIMAL / NEW.amount;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to calculate fuel efficiency
DROP TRIGGER IF EXISTS trg_calculate_fuel_efficiency ON fuel_consumption;
CREATE TRIGGER trg_calculate_fuel_efficiency
    BEFORE INSERT OR UPDATE ON fuel_consumption
    FOR EACH ROW
    EXECUTE FUNCTION calculate_fuel_efficiency();

-- Function to detect fuel theft
CREATE OR REPLACE FUNCTION detect_fuel_theft()
RETURNS TRIGGER AS $$
DECLARE
    avg_consumption DECIMAL(10, 2);
    variance DECIMAL(5, 2);
    threshold DECIMAL(5, 2);
BEGIN
    -- Get average consumption for this vehicle
    SELECT AVG(amount) INTO avg_consumption
    FROM fuel_consumption
    WHERE vehicle_id = NEW.vehicle_id
      AND fuel_type = NEW.fuel_type
      AND timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days';
    
    IF avg_consumption IS NOT NULL AND avg_consumption > 0 THEN
        -- Calculate variance
        variance = ABS((NEW.amount - avg_consumption) / avg_consumption * 100);
        
        -- Get threshold from rules
        SELECT max_consumption_variance_percentage INTO threshold
        FROM fuel_theft_rules
        WHERE is_active = TRUE
        ORDER BY priority
        LIMIT 1;
        
        -- Flag if variance exceeds threshold
        IF threshold IS NOT NULL AND variance > threshold THEN
            NEW.fuel_theft_suspected = TRUE;
            NEW.theft_detection_reason = 'Consumption variance: ' || variance::TEXT || '% exceeds threshold: ' || threshold::TEXT || '%';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to detect fuel theft
DROP TRIGGER IF EXISTS trg_detect_fuel_theft ON fuel_consumption;
CREATE TRIGGER trg_detect_fuel_theft
    BEFORE INSERT OR UPDATE ON fuel_consumption
    FOR EACH ROW
    EXECUTE FUNCTION detect_fuel_theft();

-- Insert default fuel theft detection rule
INSERT INTO fuel_theft_rules (
    rule_name,
    rule_description,
    max_consumption_variance_percentage,
    min_refill_interval_hours,
    max_refill_amount,
    sudden_drop_threshold,
    is_active,
    priority,
    auto_flag_expense,
    send_alert
) VALUES (
    'Standard Theft Detection',
    'Default rule for detecting fuel theft based on consumption patterns',
    25.0, -- 25% variance threshold
    2, -- Minimum 2 hours between refills
    100.0, -- Maximum 100 liters in single refill
    10.0, -- 10 liter sudden drop
    TRUE,
    1,
    TRUE,
    TRUE
) ON CONFLICT DO NOTHING;
