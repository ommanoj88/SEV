-- Flyway Migration V1: Create Driver Service Tables with CQRS

-- Drivers Table (Write Model)
CREATE TABLE IF NOT EXISTS drivers (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    rating DECIMAL(3, 2) DEFAULT 4.0,
    total_trips INTEGER DEFAULT 0,
    joined_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_drivers_company ON drivers (company_id);
CREATE INDEX idx_drivers_status ON drivers (status);
CREATE INDEX idx_drivers_rating ON drivers (rating DESC);

-- Driver Read Model (Materialized View for CQRS)
CREATE TABLE IF NOT EXISTS driver_read_model (
    driver_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    rating DECIMAL(3, 2),
    total_trips INTEGER,
    average_score DECIMAL(5, 2),
    total_distance DECIMAL(10, 2),
    total_hours DECIMAL(10, 2),
    current_vehicle_id VARCHAR(255),
    last_trip_date TIMESTAMP,
    performance_rank INTEGER,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_read_model_rating ON driver_read_model (rating DESC);
CREATE INDEX idx_read_model_performance ON driver_read_model (performance_rank);

-- Driver Behavior Table
CREATE TABLE IF NOT EXISTS driver_behavior (
    id VARCHAR(255) PRIMARY KEY,
    driver_id VARCHAR(255) NOT NULL,
    trip_id VARCHAR(255),
    harsh_braking INTEGER DEFAULT 0,
    harsh_acceleration INTEGER DEFAULT 0,
    overspeeding INTEGER DEFAULT 0,
    idle_time INTEGER DEFAULT 0, -- in minutes
    score INTEGER, -- 0-100
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

CREATE INDEX idx_behavior_driver ON driver_behavior (driver_id);
CREATE INDEX idx_behavior_timestamp ON driver_behavior (timestamp);
CREATE INDEX idx_behavior_score ON driver_behavior (score);

-- Driver Assignments Table
CREATE TABLE IF NOT EXISTS driver_assignments (
    id VARCHAR(255) PRIMARY KEY,
    driver_id VARCHAR(255) NOT NULL,
    vehicle_id VARCHAR(255) NOT NULL,
    shift_start TIMESTAMP NOT NULL,
    shift_end TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    assigned_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

CREATE INDEX idx_assignments_driver ON driver_assignments (driver_id);
CREATE INDEX idx_assignments_vehicle ON driver_assignments (vehicle_id);
CREATE INDEX idx_assignments_status ON driver_assignments (status);

-- Driver Attendance Table
CREATE TABLE IF NOT EXISTS driver_attendance (
    id VARCHAR(255) PRIMARY KEY,
    driver_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    total_hours DECIMAL(5, 2),
    status VARCHAR(50) DEFAULT 'PRESENT',
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id),
    UNIQUE (driver_id, date)
);

CREATE INDEX idx_attendance_driver ON driver_attendance (driver_id);
CREATE INDEX idx_attendance_date ON driver_attendance (date);
CREATE INDEX idx_attendance_status ON driver_attendance (status);

-- Driver Performance Metrics (Aggregated)
CREATE TABLE IF NOT EXISTS driver_performance_metrics (
    driver_id VARCHAR(255) PRIMARY KEY,
    overall_score DECIMAL(5, 2),
    safety_score DECIMAL(5, 2),
    efficiency_score DECIMAL(5, 2),
    punctuality_score DECIMAL(5, 2),
    total_incidents INTEGER DEFAULT 0,
    last_calculated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

-- Insert Sample Drivers
INSERT INTO drivers (id, company_id, name, license_number, phone, email, status, rating, total_trips)
VALUES
    ('DRV001', 'COMP001', 'Rajesh Kumar', 'DL1420190012345', '+91-9876543210', 'rajesh.kumar@example.com', 'ACTIVE', 4.5, 150),
    ('DRV002', 'COMP001', 'Priya Sharma', 'DL1420190054321', '+91-9876543211', 'priya.sharma@example.com', 'ACTIVE', 4.8, 200),
    ('DRV003', 'COMP001', 'Amit Patel', 'GJ0120190067890', '+91-9876543212', 'amit.patel@example.com', 'ACTIVE', 4.2, 120),
    ('DRV004', 'COMP002', 'Sneha Reddy', 'TS0920190013579', '+91-9876543213', 'sneha.reddy@example.com', 'ACTIVE', 4.6, 180),
    ('DRV005', 'COMP002', 'Vikram Singh', 'DL1420190098765', '+91-9876543214', 'vikram.singh@example.com', 'ACTIVE', 4.4, 165)
ON CONFLICT (id) DO NOTHING;

-- Insert into Read Model
INSERT INTO driver_read_model (driver_id, name, email, rating, total_trips, average_score, total_distance, total_hours, performance_rank)
SELECT
    id,
    name,
    email,
    rating,
    total_trips,
    85.0 as average_score,
    total_trips * 45.5 as total_distance,
    total_trips * 1.5 as total_hours,
    ROW_NUMBER() OVER (ORDER BY rating DESC) as performance_rank
FROM drivers
ON CONFLICT (driver_id) DO NOTHING;

-- Insert Sample Behavior Data
INSERT INTO driver_behavior (id, driver_id, trip_id, harsh_braking, harsh_acceleration, overspeeding, idle_time, score, timestamp)
VALUES
    ('BEH001', 'DRV001', 'TRIP001', 2, 1, 0, 5, 88, CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('BEH002', 'DRV002', 'TRIP002', 0, 0, 0, 3, 95, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('BEH003', 'DRV003', 'TRIP003', 3, 2, 1, 8, 75, CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

-- Function to update Read Model
CREATE OR REPLACE FUNCTION sync_driver_read_model()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO driver_read_model (driver_id, name, email, rating, total_trips, updated_at)
    VALUES (NEW.id, NEW.name, NEW.email, NEW.rating, NEW.total_trips, CURRENT_TIMESTAMP)
    ON CONFLICT (driver_id)
    DO UPDATE SET
        name = EXCLUDED.name,
        email = EXCLUDED.email,
        rating = EXCLUDED.rating,
        total_trips = EXCLUDED.total_trips,
        updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER sync_read_model_on_driver_change
AFTER INSERT OR UPDATE ON drivers
FOR EACH ROW EXECUTE FUNCTION sync_driver_read_model();

-- Update Timestamp Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_drivers_updated_at BEFORE UPDATE ON drivers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
