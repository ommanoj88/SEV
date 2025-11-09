-- V3__create_fuel_consumption_table.sql
-- PR #1: Add Vehicle Fuel Type Support
-- Creates fuel_consumption table for tracking ICE and HYBRID vehicle fuel usage
--
-- This table stores:
-- - Fuel consumption records with distance and efficiency
-- - Cost tracking for TCO calculations
-- - CO2 emissions for sustainability reporting
-- - Refueling location and timestamps

CREATE TABLE IF NOT EXISTS fuel_consumption (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    trip_id BIGINT,
    timestamp TIMESTAMP NOT NULL,
    fuel_consumed_liters DOUBLE PRECISION NOT NULL,
    distance_km DOUBLE PRECISION NOT NULL,
    fuel_efficiency_kmpl DOUBLE PRECISION,
    cost_inr DOUBLE PRECISION,
    fuel_price_per_liter DOUBLE PRECISION,
    co2_emissions_kg DOUBLE PRECISION,
    fuel_type_detail VARCHAR(50),
    refuel_location VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_fuel_vehicle_id ON fuel_consumption(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_fuel_timestamp ON fuel_consumption(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_fuel_trip_id ON fuel_consumption(trip_id);
CREATE INDEX IF NOT EXISTS idx_fuel_vehicle_timestamp ON fuel_consumption(vehicle_id, timestamp DESC);

-- Add comments for documentation
COMMENT ON TABLE fuel_consumption IS 'Tracks fuel consumption for ICE and HYBRID vehicles';
COMMENT ON COLUMN fuel_consumption.vehicle_id IS 'Reference to the vehicle';
COMMENT ON COLUMN fuel_consumption.trip_id IS 'Optional reference to associated trip';
COMMENT ON COLUMN fuel_consumption.fuel_consumed_liters IS 'Amount of fuel consumed in liters';
COMMENT ON COLUMN fuel_consumption.distance_km IS 'Distance covered in kilometers';
COMMENT ON COLUMN fuel_consumption.fuel_efficiency_kmpl IS 'Fuel efficiency in km per liter';
COMMENT ON COLUMN fuel_consumption.cost_inr IS 'Cost of fuel in Indian Rupees';
COMMENT ON COLUMN fuel_consumption.fuel_price_per_liter IS 'Fuel price per liter in INR';
COMMENT ON COLUMN fuel_consumption.co2_emissions_kg IS 'CO2 emissions in kilograms';
COMMENT ON COLUMN fuel_consumption.fuel_type_detail IS 'Type of fuel: Petrol, Diesel, CNG, etc';
