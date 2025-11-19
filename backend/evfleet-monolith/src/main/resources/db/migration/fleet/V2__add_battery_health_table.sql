-- Migration: Add battery_health table
-- Version: V2
-- Description: Add battery health tracking for EV and Hybrid vehicles

CREATE TABLE IF NOT EXISTS battery_health (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    soh DOUBLE PRECISION NOT NULL,
    cycle_count INTEGER NOT NULL,
    temperature DOUBLE PRECISION,
    internal_resistance DOUBLE PRECISION,
    voltage_deviation DOUBLE PRECISION,
    current_soc DOUBLE PRECISION,
    recorded_at TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_battery_health_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_battery_health_vehicle_id ON battery_health(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_battery_health_recorded_at ON battery_health(recorded_at);
CREATE INDEX IF NOT EXISTS idx_battery_health_vehicle_recorded ON battery_health(vehicle_id, recorded_at DESC);

-- Add comments
COMMENT ON TABLE battery_health IS 'Battery health tracking for EV and Hybrid vehicles';
COMMENT ON COLUMN battery_health.soh IS 'State of Health - Percentage of original capacity remaining (0-100%)';
COMMENT ON COLUMN battery_health.cycle_count IS 'Number of charge/discharge cycles';
COMMENT ON COLUMN battery_health.temperature IS 'Battery temperature in Celsius';
COMMENT ON COLUMN battery_health.internal_resistance IS 'Internal resistance in milli-ohms (mΩ)';
COMMENT ON COLUMN battery_health.voltage_deviation IS 'Voltage deviation from nominal (in volts)';
COMMENT ON COLUMN battery_health.current_soc IS 'Current State of Charge (0-100%)';
