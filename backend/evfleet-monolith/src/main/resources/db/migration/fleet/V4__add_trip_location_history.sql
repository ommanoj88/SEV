-- V4: Add trip location history table for path tracking and teleportation prevention
-- PR #6: Trip Validation & Teleportation Prevention

CREATE TABLE IF NOT EXISTS trip_location_history (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    speed DOUBLE PRECISION,
    distance_from_previous DOUBLE PRECISION,
    cumulative_distance DOUBLE PRECISION,
    teleportation_warning BOOLEAN DEFAULT FALSE,
    sequence_number INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_trip_location_trip
        FOREIGN KEY (trip_id)
        REFERENCES trips(id)
        ON DELETE CASCADE
);

-- Index for efficient path queries
CREATE INDEX IF NOT EXISTS idx_location_trip ON trip_location_history(trip_id);
CREATE INDEX IF NOT EXISTS idx_location_timestamp ON trip_location_history(recorded_at);
CREATE INDEX IF NOT EXISTS idx_location_sequence ON trip_location_history(trip_id, sequence_number);

COMMENT ON TABLE trip_location_history IS 'Stores complete path/route of trips for replay and teleportation detection';
COMMENT ON COLUMN trip_location_history.speed IS 'Calculated speed in km/h from previous point';
COMMENT ON COLUMN trip_location_history.distance_from_previous IS 'Distance from previous point in km';
COMMENT ON COLUMN trip_location_history.cumulative_distance IS 'Running total distance from trip start in km';
COMMENT ON COLUMN trip_location_history.teleportation_warning IS 'True if this update triggered a teleportation warning';
