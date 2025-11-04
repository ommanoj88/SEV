-- Flyway Migration V1: Create Maintenance Service Tables with Event Store

-- Event Store Table (for Event Sourcing)
CREATE TABLE IF NOT EXISTS event_store (
    event_id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    version INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255),
    UNIQUE (aggregate_id, version)
);

CREATE INDEX idx_event_aggregate ON event_store (aggregate_id);
CREATE INDEX idx_event_type ON event_store (event_type);
CREATE INDEX idx_event_timestamp ON event_store (timestamp);

-- Maintenance Schedules Table
CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    due_date DATE,
    due_mileage INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_schedules_vehicle ON maintenance_schedules (vehicle_id);
CREATE INDEX idx_schedules_due_date ON maintenance_schedules (due_date);
CREATE INDEX idx_schedules_status ON maintenance_schedules (status);

-- Service History Table
CREATE TABLE IF NOT EXISTS service_history (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    service_date DATE NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    cost DECIMAL(10, 2),
    service_center VARCHAR(255),
    description TEXT,
    parts_replaced TEXT[],
    technician VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_history_vehicle ON service_history (vehicle_id);
CREATE INDEX idx_history_date ON service_history (service_date);
CREATE INDEX idx_history_type ON service_history (service_type);

-- Battery Health Table
CREATE TABLE IF NOT EXISTS battery_health (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    soh DECIMAL(5, 2) NOT NULL, -- State of Health (0-100%)
    soc DECIMAL(5, 2) NOT NULL, -- State of Charge (0-100%)
    cycle_count INTEGER DEFAULT 0,
    temperature DECIMAL(5, 2),
    degradation_rate DECIMAL(5, 4),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_battery_vehicle ON battery_health (vehicle_id);
CREATE INDEX idx_battery_timestamp ON battery_health (timestamp);
CREATE INDEX idx_battery_soh ON battery_health (soh);

-- Warranties Table
CREATE TABLE IF NOT EXISTS warranties (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    component VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    provider VARCHAR(255),
    coverage_details TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_warranties_vehicle ON warranties (vehicle_id);
CREATE INDEX idx_warranties_end_date ON warranties (end_date);
CREATE INDEX idx_warranties_status ON warranties (status);

-- Aggregate Snapshots Table (for Event Sourcing optimization)
CREATE TABLE IF NOT EXISTS aggregate_snapshots (
    aggregate_id VARCHAR(255) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    snapshot_data JSONB NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Sample Data
INSERT INTO maintenance_schedules (id, vehicle_id, service_type, due_date, due_mileage, status, priority, description)
VALUES
    ('MAINT001', 'VEH001', 'Battery Check', CURRENT_DATE + INTERVAL '30 days', 5000, 'SCHEDULED', 'HIGH', 'Routine battery health inspection'),
    ('MAINT002', 'VEH002', 'Tire Rotation', CURRENT_DATE + INTERVAL '15 days', 10000, 'SCHEDULED', 'MEDIUM', 'Standard tire rotation service'),
    ('MAINT003', 'VEH003', 'Brake Inspection', CURRENT_DATE + INTERVAL '45 days', 15000, 'SCHEDULED', 'HIGH', 'Comprehensive brake system check')
ON CONFLICT (id) DO NOTHING;

INSERT INTO battery_health (id, vehicle_id, soh, soc, cycle_count, temperature, degradation_rate, timestamp)
VALUES
    ('BH001', 'VEH001', 95.5, 82.3, 150, 28.5, 0.0012, CURRENT_TIMESTAMP),
    ('BH002', 'VEH002', 92.8, 75.0, 220, 29.2, 0.0015, CURRENT_TIMESTAMP),
    ('BH003', 'VEH003', 98.2, 90.5, 80, 27.8, 0.0008, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Update Timestamp Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_maintenance_schedules_updated_at BEFORE UPDATE ON maintenance_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_warranties_updated_at BEFORE UPDATE ON warranties
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
