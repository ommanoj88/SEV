-- Migration: Add purchase price and energy tracking fields
-- Version: V5
-- Description: Add missing fields to vehicles and trips tables for TCO and energy tracking

-- Add purchase information to vehicles table
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='vehicles' AND column_name='purchase_price') THEN
        ALTER TABLE vehicles ADD COLUMN purchase_price NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='vehicles' AND column_name='purchase_date') THEN
        ALTER TABLE vehicles ADD COLUMN purchase_date DATE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='vehicles' AND column_name='battery_capacity') THEN
        ALTER TABLE vehicles ADD COLUMN battery_capacity NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='vehicles' AND column_name='rated_efficiency') THEN
        ALTER TABLE vehicles ADD COLUMN rated_efficiency NUMERIC(10, 4);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='vehicles' AND column_name='current_efficiency') THEN
        ALTER TABLE vehicles ADD COLUMN current_efficiency NUMERIC(10, 4);
    END IF;
END $$;

-- Add energy tracking to trips table
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='trips' AND column_name='energy_consumed') THEN
        ALTER TABLE trips ADD COLUMN energy_consumed NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='trips' AND column_name='efficiency') THEN
        ALTER TABLE trips ADD COLUMN efficiency NUMERIC(10, 4);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='trips' AND column_name='regen_energy') THEN
        ALTER TABLE trips ADD COLUMN regen_energy NUMERIC(10, 2);
    END IF;
END $$;

-- Add more details to charging_sessions table
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='charging_sessions' AND column_name='charging_power') THEN
        ALTER TABLE charging_sessions ADD COLUMN charging_power NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='charging_sessions' AND column_name='distance_since_last_charge') THEN
        ALTER TABLE charging_sessions ADD COLUMN distance_since_last_charge NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='charging_sessions' AND column_name='efficiency_since_last_charge') THEN
        ALTER TABLE charging_sessions ADD COLUMN efficiency_since_last_charge NUMERIC(10, 4);
    END IF;
END $$;

-- Add comments
COMMENT ON COLUMN vehicles.purchase_price IS 'Vehicle purchase price for TCO analysis';
COMMENT ON COLUMN vehicles.purchase_date IS 'Date vehicle was purchased';
COMMENT ON COLUMN vehicles.battery_capacity IS 'Total battery capacity in kWh (for EVs)';
COMMENT ON COLUMN vehicles.rated_efficiency IS 'Manufacturer rated efficiency kWh/100km';
COMMENT ON COLUMN vehicles.current_efficiency IS 'Real-world average efficiency kWh/100km';

COMMENT ON COLUMN trips.energy_consumed IS 'Energy used during trip in kWh';
COMMENT ON COLUMN trips.efficiency IS 'Trip efficiency kWh/100km';
COMMENT ON COLUMN trips.regen_energy IS 'Energy recovered through regenerative braking in kWh';

COMMENT ON COLUMN charging_sessions.charging_power IS 'Charging power in kW';
COMMENT ON COLUMN charging_sessions.distance_since_last_charge IS 'Distance traveled since last charge in km';
COMMENT ON COLUMN charging_sessions.efficiency_since_last_charge IS 'Calculated efficiency since last charge kWh/100km';
