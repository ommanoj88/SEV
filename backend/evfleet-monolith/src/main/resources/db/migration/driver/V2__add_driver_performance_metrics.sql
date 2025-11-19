-- Add performance tracking fields to drivers table
-- Migration for D3.PERFORMANCE_TRACKING_ANALYSIS fixes

ALTER TABLE drivers 
ADD COLUMN IF NOT EXISTS safety_score DOUBLE PRECISION DEFAULT 100.0,
ADD COLUMN IF NOT EXISTS fuel_efficiency DOUBLE PRECISION DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS harsh_braking_events INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS speeding_events INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS idling_time_minutes INTEGER DEFAULT 0;

-- Add comments for documentation
COMMENT ON COLUMN drivers.safety_score IS 'Driver safety score on a scale of 0-100';
COMMENT ON COLUMN drivers.fuel_efficiency IS 'Driver fuel efficiency in km/L or kWh/100km';
COMMENT ON COLUMN drivers.harsh_braking_events IS 'Count of harsh braking incidents';
COMMENT ON COLUMN drivers.speeding_events IS 'Count of speeding incidents';
COMMENT ON COLUMN drivers.idling_time_minutes IS 'Total idling time in minutes';
