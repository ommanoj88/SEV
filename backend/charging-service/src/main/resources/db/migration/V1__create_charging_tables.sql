-- Flyway Migration V1: Create Charging Service Tables

-- Charging Stations Table
CREATE TABLE IF NOT EXISTS charging_stations (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    available_slots INTEGER NOT NULL DEFAULT 0,
    total_slots INTEGER NOT NULL,
    charging_rate DECIMAL(10, 2),
    price_per_kwh DECIMAL(10, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    amenities TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stations_location ON charging_stations (latitude, longitude);
CREATE INDEX idx_stations_provider ON charging_stations (provider);
CREATE INDEX idx_stations_status ON charging_stations (status);

-- Charging Sessions Table
CREATE TABLE IF NOT EXISTS charging_sessions (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    station_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    energy_consumed DECIMAL(10, 3),
    cost DECIMAL(10, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (station_id) REFERENCES charging_stations(id)
);

CREATE INDEX idx_sessions_vehicle ON charging_sessions (vehicle_id);
CREATE INDEX idx_sessions_station ON charging_sessions (station_id);
CREATE INDEX idx_sessions_status ON charging_sessions (status);
CREATE INDEX idx_sessions_start_time ON charging_sessions (start_time);

-- Charging Networks Table
CREATE TABLE IF NOT EXISTS charging_networks (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(500),
    api_endpoint VARCHAR(500),
    provider VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Route Optimization Table
CREATE TABLE IF NOT EXISTS route_optimizations (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    origin_lat DECIMAL(10, 8) NOT NULL,
    origin_lon DECIMAL(11, 8) NOT NULL,
    destination_lat DECIMAL(10, 8) NOT NULL,
    destination_lon DECIMAL(11, 8) NOT NULL,
    charging_stops JSONB,
    estimated_time INTEGER,
    estimated_cost DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_route_vehicle ON route_optimizations (vehicle_id);
CREATE INDEX idx_route_created ON route_optimizations (created_at);

-- Session Reservations Table
CREATE TABLE IF NOT EXISTS session_reservations (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    station_id VARCHAR(255) NOT NULL,
    reservation_time TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (station_id) REFERENCES charging_stations(id)
);

CREATE INDEX idx_reservations_station ON session_reservations (station_id);
CREATE INDEX idx_reservations_expires ON session_reservations (expires_at);

-- Insert Sample Charging Stations
INSERT INTO charging_stations (id, name, latitude, longitude, provider, available_slots, total_slots, charging_rate, price_per_kwh, status, amenities)
VALUES
    ('STAT001', 'Tata Power - Connaught Place', 28.6289, 77.2065, 'Tata Power', 4, 4, 50.0, 12.0, 'AVAILABLE', ARRAY['WiFi', 'Restroom', 'Cafe']),
    ('STAT002', 'Statiq - Select Citywalk', 28.4941, 77.0926, 'Statiq', 6, 6, 60.0, 13.5, 'AVAILABLE', ARRAY['WiFi', 'Shopping Mall']),
    ('STAT003', 'Ather Grid - Indiranagar', 12.9784, 77.6408, 'Ather', 3, 3, 45.0, 11.5, 'AVAILABLE', ARRAY['WiFi', 'Parking']),
    ('STAT004', 'Tata Power - Gurgaon Cyber Hub', 28.4951, 77.0878, 'Tata Power', 8, 8, 50.0, 12.0, 'AVAILABLE', ARRAY['WiFi', 'Restroom', 'Food Court']),
    ('STAT005', 'Statiq - Phoenix Marketcity Mumbai', 19.0871, 72.8905, 'Statiq', 5, 5, 55.0, 13.0, 'AVAILABLE', ARRAY['WiFi', 'Shopping', 'Cinema'])
ON CONFLICT (id) DO NOTHING;

-- Update Timestamp Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_charging_stations_updated_at BEFORE UPDATE ON charging_stations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_charging_sessions_updated_at BEFORE UPDATE ON charging_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
