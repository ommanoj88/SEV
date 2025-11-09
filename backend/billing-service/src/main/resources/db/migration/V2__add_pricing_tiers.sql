-- Flyway Migration V2: Add Pricing Tiers Support
-- This migration adds support for the three-tier pricing model

-- Create pricing_tiers table to store tier configurations
CREATE TABLE IF NOT EXISTS pricing_tiers (
    tier_name VARCHAR(50) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    price_per_vehicle DECIMAL(10, 2) NOT NULL,
    description TEXT,
    features JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert the three pricing tiers
INSERT INTO pricing_tiers (tier_name, display_name, price_per_vehicle, description, features) VALUES
('BASIC', 'Basic', 299.00, 'General fleet management - Perfect for all vehicle types', 
 '["Real-time GPS tracking", "Fleet monitoring dashboard", "Driver management", "Trip history and reports", "Basic analytics", "Email support"]'::jsonb),

('EV_PREMIUM', 'EV Premium', 699.00, 'All features + EV-specific optimization',
 '["Everything in Basic", "Advanced battery health monitoring", "Smart charging optimization", "Charging station management", "Predictive maintenance", "Advanced EV analytics", "Carbon footprint tracking", "Priority support"]'::jsonb),

('ENTERPRISE', 'Enterprise', 999.00, 'Multi-depot, custom integrations, dedicated support',
 '["Everything in EV Premium", "Multi-depot management", "Custom API integrations", "White-label options", "Advanced role-based access control", "Custom reports and dashboards", "Dedicated account manager", "24/7 priority support", "SLA guarantee (99.9% uptime)"]'::jsonb);

-- Add tier column to subscriptions table (nullable for backward compatibility)
ALTER TABLE subscriptions 
ADD COLUMN IF NOT EXISTS pricing_tier VARCHAR(50);

-- Add foreign key constraint
ALTER TABLE subscriptions 
ADD CONSTRAINT fk_subscriptions_pricing_tier 
FOREIGN KEY (pricing_tier) REFERENCES pricing_tiers(tier_name);

-- Update existing subscriptions to use EV_PREMIUM tier (default for existing EV fleet)
UPDATE subscriptions 
SET pricing_tier = 'EV_PREMIUM' 
WHERE pricing_tier IS NULL;

-- Add index for pricing tier lookups
CREATE INDEX IF NOT EXISTS idx_subscriptions_pricing_tier ON subscriptions(pricing_tier);

-- Update pricing_plans table to reference pricing_tier (if using plan_id as tier reference)
ALTER TABLE pricing_plans 
ADD COLUMN IF NOT EXISTS pricing_tier VARCHAR(50);

ALTER TABLE pricing_plans 
ADD CONSTRAINT fk_pricing_plans_tier 
FOREIGN KEY (pricing_tier) REFERENCES pricing_tiers(tier_name);

-- Create a view for active pricing with tier information
CREATE OR REPLACE VIEW v_pricing_tiers_with_features AS
SELECT 
    pt.tier_name,
    pt.display_name,
    pt.price_per_vehicle,
    pt.description,
    pt.features,
    COUNT(DISTINCT s.id) as active_subscriptions,
    SUM(s.vehicle_count) as total_vehicles
FROM pricing_tiers pt
LEFT JOIN subscriptions s ON s.pricing_tier = pt.tier_name AND s.status = 'ACTIVE'
WHERE pt.is_active = true
GROUP BY pt.tier_name, pt.display_name, pt.price_per_vehicle, pt.description, pt.features
ORDER BY pt.price_per_vehicle;
