-- EVFleet Monolith - Seed Data for Testing
-- This script populates the databases with sample data for testing
-- Run this AFTER the application has created the tables (first startup)

-- ========== AUTH DATABASE ==========
\c evfleet_auth

-- Insert roles
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
(1, 'ROLE_ADMIN', 'Administrator with full access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'ROLE_FLEET_MANAGER', 'Fleet manager with vehicle management access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'ROLE_DRIVER', 'Driver with limited access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'ROLE_VIEWER', 'Read-only access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Note: Users are created via Firebase, so no seed data here
-- They will be synced to database when they register

-- ========== FLEET DATABASE ==========
\c evfleet_fleet

-- Sample vehicles (for company_id = 1)
-- IMPORTANT: All 4-wheeler vehicles (cars/LCVs) should have type='LCV'
-- Only 2-wheelers (motorcycles/scooters) should have type='TWO_WHEELER'
INSERT INTO vehicles (company_id, vehicle_number, type, make, model, year, fuel_type, battery_capacity, current_battery_soc, fuel_tank_capacity, fuel_level, latitude, longitude, status, created_at, updated_at) VALUES
(1, 'MH12AB1234', 'LCV', 'Tata', 'Nexon EV', 2023, 'EV', 40.5, 85.0, NULL, NULL, 19.0760, 72.8777, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'MH14CD5678', 'LCV', 'MG', 'ZS EV', 2023, 'EV', 50.3, 92.0, NULL, NULL, 19.0820, 72.8850, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'MH12EF9012', 'LCV', 'Mahindra', 'XUV700', 2023, 'ICE', NULL, NULL, 60.0, 45.5, 19.0700, 72.8700, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'MH14GH3456', 'LCV', 'Toyota', 'Innova Hycross', 2024, 'HYBRID', 1.8, 100.0, 52.0, 40.0, 19.0850, 72.8900, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== CHARGING DATABASE ==========
\c evfleet_charging

-- Sample charging stations
INSERT INTO charging_stations (name, location, latitude, longitude, total_slots, available_slots, price_per_kwh, status, created_at, updated_at) VALUES
('PowerHub Andheri Station', 'Andheri West, Mumbai', 19.1197, 72.8464, 10, 10, 12.50, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ChargeZone BKC', 'Bandra Kurla Complex, Mumbai', 19.0653, 72.8678, 15, 15, 15.00, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EV Point Powai', 'Powai, Mumbai', 19.1176, 72.9060, 8, 8, 11.00, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('QuickCharge Worli', 'Worli, Mumbai', 19.0144, 72.8186, 12, 12, 13.50, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== DRIVER DATABASE ==========
\c evfleet_driver

-- Sample drivers (for company_id = 1)
INSERT INTO drivers (company_id, name, phone, email, license_number, license_expiry, status, total_trips, total_distance, created_at, updated_at) VALUES
(1, 'Rajesh Kumar', '9876543210', 'rajesh.kumar@example.com', 'MH1220230001', '2026-12-31', 'ACTIVE', 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Amit Sharma', '9876543211', 'amit.sharma@example.com', 'MH1220230002', '2027-06-30', 'ACTIVE', 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Priya Desai', '9876543212', 'priya.desai@example.com', 'MH1420230003', '2026-09-15', 'ACTIVE', 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Suresh Patil', '9876543213', 'suresh.patil@example.com', 'MH1220230004', '2025-12-20', 'ACTIVE', 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== ANALYTICS DATABASE ==========
\c evfleet_analytics

-- Sample fleet summary for today
INSERT INTO fleet_summaries (company_id, summary_date, total_vehicles, active_vehicles, total_trips, total_distance, total_energy_consumed, total_cost, created_at, updated_at) VALUES
(1, CURRENT_DATE, 4, 4, 0, 0.0, 0.0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== BILLING DATABASE ==========
\c evfleet_billing

-- Sample pricing plans
INSERT INTO pricing_plans (name, description, price_per_vehicle, min_vehicles, max_vehicles, billing_cycle, is_active, has_analytics, has_maintenance_tracking, has_driver_management, has_charging_management, has_priority_support, has_api_access, created_at, updated_at) VALUES
('BASIC', 'Basic plan for small fleets', 299.00, 1, 10, 'MONTHLY', true, true, true, true, true, false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PRO', 'Professional plan for growing fleets', 249.00, 11, 50, 'MONTHLY', true, true, true, true, true, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ENTERPRISE', 'Enterprise plan for large fleets', 199.00, 51, NULL, 'MONTHLY', true, true, true, true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Sample subscription for company 1
INSERT INTO subscriptions (company_id, plan_type, vehicle_count, amount, billing_cycle, start_date, status, auto_renew, created_at, updated_at) VALUES
(1, 'BASIC', 4, 1196.00, 'MONTHLY', CURRENT_DATE, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== NOTIFICATION DATABASE ==========
\c evfleet_notification

-- No seed data for notifications (they are created dynamically)

-- ========== MAINTENANCE DATABASE ==========
\c evfleet_maintenance

-- No seed data for maintenance records (they are created as needed)

-- Summary
SELECT '✅ Seed data inserted successfully!' AS result;
SELECT '📊 Sample Data Created:' AS info;
SELECT '   - 4 Roles (auth)' AS info;
SELECT '   - 4 Vehicles (fleet)' AS info;
SELECT '   - 4 Charging Stations' AS info;
SELECT '   - 4 Drivers' AS info;
SELECT '   - 3 Pricing Plans' AS info;
SELECT '   - 1 Active Subscription' AS info;
SELECT '   - 1 Fleet Summary Entry' AS info;
