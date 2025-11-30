-- =====================================================
-- EV Fleet Management - Test Seed Data
-- User: testuser1@gmail.com / Password@123
-- =====================================================

-- Clear existing test data
DELETE FROM fleet_summaries WHERE company_id = 1;
DELETE FROM maintenance_records WHERE vehicle_id IN (SELECT id FROM vehicles WHERE company_id = 1);
DELETE FROM trips WHERE company_id = 1;
DELETE FROM charging_sessions WHERE vehicle_id IN (SELECT id FROM vehicles WHERE company_id = 1);
DELETE FROM driver_assignments WHERE driver_id IN (SELECT id FROM drivers WHERE company_id = 1);
DELETE FROM drivers WHERE company_id = 1;
DELETE FROM vehicles WHERE company_id = 1;
DELETE FROM subscriptions WHERE company_id = 1;
DELETE FROM notifications WHERE user_id IN (SELECT id FROM users WHERE company_id = 1);
DELETE FROM geofences WHERE company_id = 1;
DELETE FROM routes WHERE company_id = 1;
DELETE FROM users WHERE company_id = 1;
DELETE FROM companies WHERE id = 1;

-- =====================================================
-- 1. COMPANY & ROLES
-- =====================================================
INSERT INTO companies (id, name, email, phone, address, created_at, updated_at) VALUES
(1, 'Test Fleet Company', 'admin@testfleet.com', '+91-9876543210', 'Mumbai, India', NOW(), NOW());

-- Roles (if not exists)
INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'System Administrator'),
(2, 'FLEET_MANAGER', 'Fleet Manager'),
(3, 'DRIVER', 'Vehicle Driver'),
(4, 'VIEWER', 'Read-only access')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 2. TEST USERS
-- =====================================================
-- Password: Password@123 (bcrypt hash)
INSERT INTO users (id, email, password_hash, name, phone, role_id, company_id, firebase_uid, status, created_at, updated_at) VALUES
(1, 'testuser1@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3NPHwNNRGLyRRtWBXMha', 'Test User One', '+91-9876543211', 2, 1, 'firebase_test_uid_1', 'ACTIVE', NOW(), NOW()),
(2, 'testadmin@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3NPHwNNRGLyRRtWBXMha', 'Test Admin', '+91-9876543212', 1, 1, 'firebase_test_uid_2', 'ACTIVE', NOW(), NOW()),
(3, 'testdriver@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3NPHwNNRGLyRRtWBXMha', 'Test Driver', '+91-9876543213', 3, 1, 'firebase_test_uid_3', 'ACTIVE', NOW(), NOW()),
(4, 'testviewer@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3NPHwNNRGLyRRtWBXMha', 'Test Viewer', '+91-9876543214', 4, 1, 'firebase_test_uid_4', 'ACTIVE', NOW(), NOW());

-- =====================================================
-- 3. VEHICLES (Various types and statuses)
-- =====================================================
INSERT INTO vehicles (id, company_id, license_plate, vin, make, model, year, fuel_type, status, current_latitude, current_longitude, battery_capacity_kwh, current_battery_soc, current_range_km, odometer_km, created_at, updated_at) VALUES
-- Electric Vehicles
(1, 1, 'MH01EV0001', 'VIN00000000000001', 'Tesla', 'Model 3', 2023, 'EV', 'AVAILABLE', 19.0760, 72.8777, 75.0, 85, 350, 15000, NOW(), NOW()),
(2, 1, 'MH01EV0002', 'VIN00000000000002', 'Tata', 'Nexon EV', 2023, 'EV', 'IN_USE', 19.0825, 72.8925, 40.5, 65, 180, 22000, NOW(), NOW()),
(3, 1, 'MH01EV0003', 'VIN00000000000003', 'MG', 'ZS EV', 2024, 'EV', 'CHARGING', 19.0500, 72.8300, 50.3, 25, 80, 8000, NOW(), NOW()),
(4, 1, 'MH01EV0004', 'VIN00000000000004', 'Hyundai', 'Kona Electric', 2023, 'EV', 'MAINTENANCE', 19.1000, 72.9000, 64.0, 0, 0, 35000, NOW(), NOW()),
-- ICE Vehicles
(5, 1, 'MH01IC0001', 'VIN00000000000005', 'Maruti', 'Swift Dzire', 2022, 'ICE', 'AVAILABLE', 19.0600, 72.8500, NULL, NULL, 400, 45000, NOW(), NOW()),
(6, 1, 'MH01IC0002', 'VIN00000000000006', 'Honda', 'City', 2023, 'ICE', 'IN_USE', 19.0700, 72.8600, NULL, NULL, 550, 28000, NOW(), NOW()),
-- Hybrid Vehicles
(7, 1, 'MH01HY0001', 'VIN00000000000007', 'Toyota', 'Camry Hybrid', 2023, 'HYBRID', 'AVAILABLE', 19.0800, 72.8700, 4.4, 60, 800, 18000, NOW(), NOW()),
(8, 1, 'MH01HY0002', 'VIN00000000000008', 'Honda', 'City e:HEV', 2024, 'HYBRID', 'INACTIVE', 19.0900, 72.8800, 1.5, 40, 700, 5000, NOW(), NOW());

-- =====================================================
-- 4. DRIVERS (Various statuses)
-- =====================================================
INSERT INTO drivers (id, company_id, name, email, phone, license_number, license_expiry, status, created_at, updated_at) VALUES
(1, 1, 'Rahul Sharma', 'rahul.sharma@testfleet.com', '+91-9876500001', 'MH0120230001234', '2025-12-31', 'ACTIVE', NOW(), NOW()),
(2, 1, 'Priya Patel', 'priya.patel@testfleet.com', '+91-9876500002', 'MH0120230001235', '2024-06-30', 'ACTIVE', NOW(), NOW()),
(3, 1, 'Amit Kumar', 'amit.kumar@testfleet.com', '+91-9876500003', 'MH0120230001236', '2023-12-15', 'ACTIVE', NOW(), NOW()), -- Expired license
(4, 1, 'Sneha Desai', 'sneha.desai@testfleet.com', '+91-9876500004', 'MH0120230001237', '2026-03-15', 'ON_TRIP', NOW(), NOW()),
(5, 1, 'Vikram Singh', 'vikram.singh@testfleet.com', '+91-9876500005', 'MH0120230001238', '2025-08-20', 'OFF_DUTY', NOW(), NOW()),
(6, 1, 'Anita Joshi', 'anita.joshi@testfleet.com', '+91-9876500006', 'MH0120230001239', '2025-11-10', 'INACTIVE', NOW(), NOW());

-- =====================================================
-- 5. DRIVER ASSIGNMENTS
-- =====================================================
INSERT INTO driver_assignments (driver_id, vehicle_id, assigned_at, is_active) VALUES
(1, 1, NOW() - INTERVAL '30 days', true),
(4, 2, NOW() - INTERVAL '5 days', true),
(2, 5, NOW() - INTERVAL '15 days', true);

-- =====================================================
-- 6. CHARGING STATIONS
-- =====================================================
INSERT INTO charging_stations (id, company_id, name, address, latitude, longitude, total_slots, available_slots, price_per_kwh, status, connector_types, created_at, updated_at) VALUES
(1, 1, 'Mumbai Central Station', 'Near Mumbai Central Railway Station', 18.9690, 72.8193, 10, 7, 12.50, 'ACTIVE', '["CCS2", "CHAdeMO", "Type2"]', NOW(), NOW()),
(2, 1, 'Andheri East Hub', 'Andheri East Metro Station', 19.1197, 72.8464, 6, 6, 14.00, 'ACTIVE', '["CCS2", "Type2"]', NOW(), NOW()),
(3, 1, 'BKC Fast Charge', 'Bandra Kurla Complex', 19.0596, 72.8650, 8, 2, 18.00, 'ACTIVE', '["CCS2", "CHAdeMO"]', NOW(), NOW()),
(4, 1, 'Powai Tech Park', 'Hiranandani Gardens, Powai', 19.1176, 72.9060, 4, 0, 15.00, 'ACTIVE', '["Type2"]', NOW(), NOW()),
(5, 1, 'Maintenance Closed Station', 'Under Repair Location', 19.0500, 72.8000, 5, 0, 12.00, 'INACTIVE', '["CCS2"]', NOW(), NOW());

-- =====================================================
-- 7. CHARGING SESSIONS
-- =====================================================
INSERT INTO charging_sessions (id, vehicle_id, station_id, start_time, end_time, energy_kwh, cost, status, created_at) VALUES
(1, 1, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '45 minutes', 35.5, 443.75, 'COMPLETED', NOW()),
(2, 2, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '30 minutes', 20.0, 280.00, 'COMPLETED', NOW()),
(3, 3, 3, NOW() - INTERVAL '2 hours', NULL, NULL, NULL, 'IN_PROGRESS', NOW()),
(4, 1, 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '60 minutes', 50.0, 625.00, 'COMPLETED', NOW());

-- =====================================================
-- 8. TRIPS
-- =====================================================
INSERT INTO trips (id, company_id, vehicle_id, driver_id, start_time, end_time, start_location, end_location, start_latitude, start_longitude, end_latitude, end_longitude, distance_km, status, created_at) VALUES
(1, 1, 1, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '2 hours', 'Mumbai Central', 'Andheri West', 18.9690, 72.8193, 19.1364, 72.8296, 25.5, 'COMPLETED', NOW()),
(2, 1, 2, 4, NOW() - INTERVAL '1 hour', NULL, 'BKC', NULL, 19.0596, 72.8650, NULL, NULL, NULL, 'IN_PROGRESS', NOW()),
(3, 1, 5, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '3 hours', 'Powai', 'Thane', 19.1176, 72.9060, 19.2183, 72.9781, 18.3, 'COMPLETED', NOW()),
(4, 1, 6, NULL, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '90 minutes', 'Dadar', 'Worli', 19.0178, 72.8478, 19.0176, 72.8171, 8.2, 'COMPLETED', NOW());

-- =====================================================
-- 9. MAINTENANCE RECORDS
-- =====================================================
INSERT INTO maintenance_records (id, vehicle_id, type, description, scheduled_date, completed_date, cost, status, odometer_at_service, created_at) VALUES
(1, 1, 'ROUTINE', 'Regular service - tire rotation, brake check', NOW() + INTERVAL '7 days', NULL, 5000.00, 'SCHEDULED', 15000, NOW()),
(2, 2, 'BATTERY_CHECK', 'Annual battery health assessment', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', 2000.00, 'COMPLETED', 20000, NOW()),
(3, 4, 'REPAIR', 'Motor controller replacement', NOW() - INTERVAL '2 days', NULL, 45000.00, 'IN_PROGRESS', 35000, NOW()),
(4, 5, 'OIL_CHANGE', 'Routine oil and filter change', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days', 3500.00, 'COMPLETED', 42000, NOW()),
(5, 3, 'TIRE_REPLACEMENT', 'Front tire replacement', NOW() + INTERVAL '14 days', NULL, 12000.00, 'SCHEDULED', 8000, NOW()),
(6, 1, 'BRAKE_SERVICE', 'Brake pad replacement - overdue', NOW() - INTERVAL '10 days', NULL, 8000.00, 'OVERDUE', 14500, NOW());

-- =====================================================
-- 10. NOTIFICATIONS
-- =====================================================
INSERT INTO notifications (id, user_id, title, message, type, priority, is_read, created_at) VALUES
(1, 1, 'Maintenance Due', 'Vehicle MH01EV0001 maintenance is overdue', 'MAINTENANCE', 'HIGH', false, NOW() - INTERVAL '1 hour'),
(2, 1, 'Low Battery Alert', 'Vehicle MH01EV0003 battery below 30%', 'BATTERY', 'MEDIUM', false, NOW() - INTERVAL '2 hours'),
(3, 1, 'Trip Completed', 'Trip #1 completed successfully', 'TRIP', 'LOW', true, NOW() - INTERVAL '3 days'),
(4, 1, 'License Expiry Warning', 'Driver Amit Kumar license expires in 15 days', 'DRIVER', 'HIGH', false, NOW() - INTERVAL '1 day'),
(5, 1, 'Charging Complete', 'Vehicle MH01EV0002 charging completed at Andheri East Hub', 'CHARGING', 'LOW', true, NOW() - INTERVAL '1 day'),
(6, 2, 'New Driver Added', 'Driver Vikram Singh has been added to the fleet', 'SYSTEM', 'LOW', true, NOW() - INTERVAL '7 days');

-- =====================================================
-- 11. GEOFENCES
-- =====================================================
INSERT INTO geofences (id, company_id, name, description, type, coordinates, radius_meters, is_active, alert_on_entry, alert_on_exit, created_at) VALUES
(1, 1, 'Mumbai Office', 'Main office perimeter', 'CIRCLE', '{"lat": 19.0760, "lng": 72.8777}', 500, true, true, true, NOW()),
(2, 1, 'Andheri Depot', 'Vehicle depot area', 'CIRCLE', '{"lat": 19.1197, "lng": 72.8464}', 300, true, false, true, NOW()),
(3, 1, 'BKC Zone', 'Business district zone', 'POLYGON', '[{"lat": 19.055, "lng": 72.860}, {"lat": 19.065, "lng": 72.860}, {"lat": 19.065, "lng": 72.875}, {"lat": 19.055, "lng": 72.875}]', NULL, true, true, false, NOW()),
(4, 1, 'Restricted Area', 'No-go zone for testing', 'CIRCLE', '{"lat": 19.0500, "lng": 72.8000}', 200, false, true, true, NOW());

-- =====================================================
-- 12. ROUTES
-- =====================================================
INSERT INTO routes (id, company_id, name, description, waypoints, distance_km, estimated_duration_mins, is_active, created_at) VALUES
(1, 1, 'Daily Office Route', 'Standard route from depot to office', '[{"lat": 19.1197, "lng": 72.8464}, {"lat": 19.0900, "lng": 72.8600}, {"lat": 19.0760, "lng": 72.8777}]', 12.5, 35, true, NOW()),
(2, 1, 'BKC Shuttle', 'Shuttle service to BKC', '[{"lat": 19.0760, "lng": 72.8777}, {"lat": 19.0596, "lng": 72.8650}]', 5.2, 15, true, NOW()),
(3, 1, 'Airport Transfer', 'Route to Mumbai Airport', '[{"lat": 19.0760, "lng": 72.8777}, {"lat": 19.0896, "lng": 72.8656}]', 18.8, 45, true, NOW());

-- =====================================================
-- 13. PRICING PLANS & SUBSCRIPTIONS
-- =====================================================
INSERT INTO pricing_plans (id, name, description, price_monthly, price_yearly, max_vehicles, max_drivers, features, is_active, created_at) VALUES
(1, 'BASIC', 'Basic fleet management', 999.00, 9999.00, 10, 10, '["Vehicle Tracking", "Driver Management", "Basic Reports"]', true, NOW()),
(2, 'PRO', 'Professional fleet management', 2999.00, 29999.00, 50, 50, '["All Basic Features", "Maintenance Scheduling", "Advanced Analytics", "API Access"]', true, NOW()),
(3, 'ENTERPRISE', 'Enterprise fleet management', 9999.00, 99999.00, -1, -1, '["All Pro Features", "Unlimited Vehicles", "Custom Integrations", "Priority Support"]', true, NOW());

INSERT INTO subscriptions (id, company_id, plan_id, status, start_date, end_date, created_at) VALUES
(1, 1, 2, 'ACTIVE', NOW() - INTERVAL '6 months', NOW() + INTERVAL '6 months', NOW());

-- =====================================================
-- 14. FLEET SUMMARY (Analytics data)
-- =====================================================
INSERT INTO fleet_summaries (id, company_id, date, total_vehicles, active_vehicles, total_drivers, active_drivers, total_trips, total_distance_km, total_energy_kwh, total_charging_cost, created_at) VALUES
(1, 1, CURRENT_DATE - 1, 8, 6, 6, 4, 5, 127.5, 105.5, 1348.75, NOW()),
(2, 1, CURRENT_DATE - 2, 8, 7, 6, 5, 8, 185.2, 142.0, 1775.00, NOW()),
(3, 1, CURRENT_DATE - 3, 8, 6, 6, 4, 6, 156.8, 118.5, 1481.25, NOW()),
(4, 1, CURRENT_DATE, 8, 5, 6, 3, 2, 25.5, 20.0, 280.00, NOW());

-- =====================================================
-- 15. BATTERY HEALTH RECORDS
-- =====================================================
INSERT INTO battery_health_records (id, vehicle_id, recorded_at, soh_percentage, cycle_count, degradation_rate, notes, created_at) VALUES
(1, 1, NOW() - INTERVAL '30 days', 98.5, 120, 0.012, 'Excellent condition', NOW()),
(2, 1, NOW(), 98.2, 135, 0.013, 'Normal degradation', NOW()),
(3, 2, NOW() - INTERVAL '30 days', 95.0, 350, 0.018, 'Good condition', NOW()),
(4, 2, NOW(), 94.5, 380, 0.019, 'Slight increase in degradation', NOW()),
(5, 3, NOW(), 99.0, 50, 0.008, 'New vehicle - excellent health', NOW()),
(6, 4, NOW() - INTERVAL '7 days', 78.0, 800, 0.035, 'Battery replacement recommended', NOW());

-- Reset sequences
SELECT setval('companies_id_seq', (SELECT MAX(id) FROM companies));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('vehicles_id_seq', (SELECT MAX(id) FROM vehicles));
SELECT setval('drivers_id_seq', (SELECT MAX(id) FROM drivers));
SELECT setval('charging_stations_id_seq', (SELECT MAX(id) FROM charging_stations));
SELECT setval('charging_sessions_id_seq', (SELECT MAX(id) FROM charging_sessions));
SELECT setval('trips_id_seq', (SELECT MAX(id) FROM trips));
SELECT setval('maintenance_records_id_seq', (SELECT MAX(id) FROM maintenance_records));
SELECT setval('notifications_id_seq', (SELECT MAX(id) FROM notifications));
SELECT setval('geofences_id_seq', (SELECT MAX(id) FROM geofences));
SELECT setval('routes_id_seq', (SELECT MAX(id) FROM routes));
SELECT setval('pricing_plans_id_seq', (SELECT MAX(id) FROM pricing_plans));
SELECT setval('subscriptions_id_seq', (SELECT MAX(id) FROM subscriptions));
SELECT setval('fleet_summaries_id_seq', (SELECT MAX(id) FROM fleet_summaries));
SELECT setval('battery_health_records_id_seq', (SELECT MAX(id) FROM battery_health_records));

-- Verify seed data
SELECT 'Seed data loaded successfully!' AS status;
SELECT 'Companies: ' || COUNT(*) FROM companies;
SELECT 'Users: ' || COUNT(*) FROM users;
SELECT 'Vehicles: ' || COUNT(*) FROM vehicles;
SELECT 'Drivers: ' || COUNT(*) FROM drivers;
SELECT 'Charging Stations: ' || COUNT(*) FROM charging_stations;
SELECT 'Trips: ' || COUNT(*) FROM trips;
SELECT 'Maintenance Records: ' || COUNT(*) FROM maintenance_records;
SELECT 'Notifications: ' || COUNT(*) FROM notifications;
