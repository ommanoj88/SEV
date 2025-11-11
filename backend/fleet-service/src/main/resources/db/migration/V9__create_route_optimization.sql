-- Route Optimization Tables

-- Route Plans Table
CREATE TABLE route_plans (
    id BIGSERIAL PRIMARY KEY,
    
    -- Route Details
    route_name VARCHAR(255) NOT NULL,
    route_description TEXT,
    
    -- Assignment
    vehicle_id BIGINT,
    driver_id BIGINT,
    
    -- Route Points
    origin_lat DECIMAL(10, 8) NOT NULL,
    origin_lng DECIMAL(11, 8) NOT NULL,
    origin_address TEXT,
    
    destination_lat DECIMAL(10, 8) NOT NULL,
    destination_lng DECIMAL(11, 8) NOT NULL,
    destination_address TEXT,
    
    -- Route Optimization
    total_distance DECIMAL(10, 2), -- km
    estimated_duration INT, -- minutes
    estimated_fuel_consumption DECIMAL(10, 2),
    estimated_cost DECIMAL(10, 2),
    
    -- Time Windows
    planned_start_time TIMESTAMP,
    planned_end_time TIMESTAMP,
    
    -- Optimization Parameters
    optimization_criteria VARCHAR(50), -- 'DISTANCE', 'TIME', 'FUEL', 'COST'
    traffic_considered BOOLEAN DEFAULT TRUE,
    toll_roads_allowed BOOLEAN DEFAULT TRUE,
    
    -- Status
    status VARCHAR(50) DEFAULT 'PLANNED', -- 'PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    
    -- Actual Performance (after completion)
    actual_distance DECIMAL(10, 2),
    actual_duration INT,
    actual_fuel_consumption DECIMAL(10, 2),
    actual_cost DECIMAL(10, 2),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT chk_coordinates_origin CHECK (
        origin_lat BETWEEN -90 AND 90 AND origin_lng BETWEEN -180 AND 180
    ),
    CONSTRAINT chk_coordinates_destination CHECK (
        destination_lat BETWEEN -90 AND 90 AND destination_lng BETWEEN -180 AND 180
    )
);

-- Route Waypoints Table (for multi-stop routes)
CREATE TABLE route_waypoints (
    id BIGSERIAL PRIMARY KEY,
    route_plan_id BIGINT NOT NULL REFERENCES route_plans(id) ON DELETE CASCADE,
    
    -- Waypoint Details
    sequence_number INT NOT NULL,
    waypoint_name VARCHAR(255),
    
    -- Location
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address TEXT,
    
    -- Time Window
    arrival_window_start TIMESTAMP,
    arrival_window_end TIMESTAMP,
    planned_arrival_time TIMESTAMP,
    actual_arrival_time TIMESTAMP,
    
    -- Stop Details
    stop_duration INT, -- minutes
    service_type VARCHAR(100), -- 'PICKUP', 'DELIVERY', 'SERVICE', 'REST'
    
    -- Customer Information (if applicable)
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    customer_email VARCHAR(100),
    
    -- Delivery/Pickup Details
    items_description TEXT,
    weight DECIMAL(10, 2),
    volume DECIMAL(10, 2),
    
    -- Proof of Delivery
    pod_signature_path VARCHAR(500),
    pod_photo_path VARCHAR(500),
    pod_notes TEXT,
    pod_timestamp TIMESTAMP,
    pod_captured_by VARCHAR(255),
    
    -- Status
    status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'IN_TRANSIT', 'COMPLETED', 'FAILED', 'SKIPPED'
    completion_notes TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_waypoint_coordinates CHECK (
        latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180
    ),
    CONSTRAINT unique_route_sequence UNIQUE (route_plan_id, sequence_number)
);

-- Route Optimization History Table
CREATE TABLE route_optimization_history (
    id BIGSERIAL PRIMARY KEY,
    route_plan_id BIGINT NOT NULL REFERENCES route_plans(id) ON DELETE CASCADE,
    
    -- Optimization Details
    optimization_algorithm VARCHAR(100), -- 'GENETIC', 'ANT_COLONY', 'DIJKSTRA', 'A_STAR'
    optimization_criteria VARCHAR(50),
    
    -- Results
    original_distance DECIMAL(10, 2),
    optimized_distance DECIMAL(10, 2),
    distance_saved DECIMAL(10, 2),
    time_saved INT, -- minutes
    cost_saved DECIMAL(10, 2),
    
    -- Optimization Parameters
    traffic_data_used BOOLEAN,
    weather_data_used BOOLEAN,
    vehicle_capacity_considered BOOLEAN,
    time_windows_considered BOOLEAN,
    
    -- Performance
    optimization_duration_ms BIGINT,
    iterations_count INT,
    
    -- Audit
    optimized_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    optimized_by VARCHAR(255)
);

-- Traffic Data Cache Table
CREATE TABLE traffic_data_cache (
    id BIGSERIAL PRIMARY KEY,
    
    -- Location
    start_lat DECIMAL(10, 8) NOT NULL,
    start_lng DECIMAL(11, 8) NOT NULL,
    end_lat DECIMAL(10, 8) NOT NULL,
    end_lng DECIMAL(11, 8) NOT NULL,
    
    -- Traffic Information
    current_speed DECIMAL(5, 2), -- km/h
    free_flow_speed DECIMAL(5, 2), -- km/h
    congestion_level VARCHAR(20), -- 'LOW', 'MEDIUM', 'HIGH', 'SEVERE'
    travel_time INT, -- seconds
    
    -- Timestamp
    data_timestamp TIMESTAMP NOT NULL,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    -- Source
    data_source VARCHAR(100) -- 'GOOGLE_MAPS', 'HERE_MAPS', 'TomTom', etc.
);

-- Customer Management Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    
    -- Basic Information
    customer_code VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_type VARCHAR(50), -- 'INDIVIDUAL', 'BUSINESS'
    
    -- Contact Information
    primary_contact_name VARCHAR(255),
    primary_phone VARCHAR(20),
    secondary_phone VARCHAR(20),
    email VARCHAR(100),
    
    -- Address
    address_line1 TEXT,
    address_line2 TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'India',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    -- Business Details
    gstin VARCHAR(15),
    pan VARCHAR(10),
    
    -- Preferences
    preferred_delivery_time VARCHAR(50),
    special_instructions TEXT,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    credit_limit DECIMAL(12, 2),
    outstanding_balance DECIMAL(12, 2) DEFAULT 0,
    
    -- Ratings
    service_rating DECIMAL(3, 2), -- Average rating
    total_deliveries INT DEFAULT 0,
    successful_deliveries INT DEFAULT 0,
    failed_deliveries INT DEFAULT 0,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Customer Feedback Table
CREATE TABLE customer_feedback (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    waypoint_id BIGINT REFERENCES route_waypoints(id) ON DELETE SET NULL,
    route_plan_id BIGINT REFERENCES route_plans(id) ON DELETE SET NULL,
    
    -- Feedback Details
    rating INT CHECK (rating BETWEEN 1 AND 5),
    feedback_text TEXT,
    feedback_category VARCHAR(50), -- 'DELIVERY_QUALITY', 'DRIVER_BEHAVIOR', 'TIMELINESS', 'COMMUNICATION'
    
    -- Response
    is_addressed BOOLEAN DEFAULT FALSE,
    response_text TEXT,
    responded_by VARCHAR(255),
    responded_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Indexes for performance
CREATE INDEX idx_route_plans_vehicle ON route_plans(vehicle_id);
CREATE INDEX idx_route_plans_driver ON route_plans(driver_id);
CREATE INDEX idx_route_plans_status ON route_plans(status);
CREATE INDEX idx_route_plans_start_time ON route_plans(planned_start_time);

CREATE INDEX idx_route_waypoints_route ON route_waypoints(route_plan_id);
CREATE INDEX idx_route_waypoints_sequence ON route_waypoints(route_plan_id, sequence_number);
CREATE INDEX idx_route_waypoints_status ON route_waypoints(status);
CREATE INDEX idx_route_waypoints_location ON route_waypoints(latitude, longitude);

CREATE INDEX idx_route_optimization_history_route ON route_optimization_history(route_plan_id);
CREATE INDEX idx_route_optimization_history_date ON route_optimization_history(optimized_at);

CREATE INDEX idx_traffic_data_cache_location ON traffic_data_cache(start_lat, start_lng, end_lat, end_lng);
CREATE INDEX idx_traffic_data_cache_timestamp ON traffic_data_cache(data_timestamp);
CREATE INDEX idx_traffic_data_cache_expires ON traffic_data_cache(expires_at);

CREATE INDEX idx_customers_code ON customers(customer_code);
CREATE INDEX idx_customers_name ON customers(customer_name);
CREATE INDEX idx_customers_location ON customers(latitude, longitude);
CREATE INDEX idx_customers_active ON customers(is_active);

CREATE INDEX idx_customer_feedback_customer ON customer_feedback(customer_id);
CREATE INDEX idx_customer_feedback_waypoint ON customer_feedback(waypoint_id);
CREATE INDEX idx_customer_feedback_rating ON customer_feedback(rating);

-- Function to generate customer code
CREATE OR REPLACE FUNCTION generate_customer_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.customer_code IS NULL THEN
        NEW.customer_code = 'CUST-' || LPAD(NEW.id::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to generate customer code
CREATE TRIGGER trg_generate_customer_code
    BEFORE INSERT ON customers
    FOR EACH ROW
    EXECUTE FUNCTION generate_customer_code();

-- Function to update customer rating
CREATE OR REPLACE FUNCTION update_customer_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE customers
    SET service_rating = (
        SELECT AVG(rating)
        FROM customer_feedback
        WHERE customer_id = NEW.customer_id
    )
    WHERE id = NEW.customer_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update customer rating
CREATE TRIGGER trg_update_customer_rating
    AFTER INSERT OR UPDATE ON customer_feedback
    FOR EACH ROW
    EXECUTE FUNCTION update_customer_rating();
