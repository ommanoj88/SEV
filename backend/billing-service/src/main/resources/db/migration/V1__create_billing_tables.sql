-- Flyway Migration V1: Create Billing Service Tables with Event Sourcing

-- Event Store Table (for Event Sourcing)
CREATE TABLE IF NOT EXISTS event_store (
    event_id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    version INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255),
    UNIQUE (aggregate_id, version)
);

CREATE INDEX idx_billing_event_aggregate ON event_store (aggregate_id);
CREATE INDEX idx_billing_event_type ON event_store (event_type);
CREATE INDEX idx_billing_event_timestamp ON event_store (timestamp);

-- Subscriptions Table
CREATE TABLE IF NOT EXISTS subscriptions (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    plan_type VARCHAR(100) NOT NULL,
    vehicle_count INTEGER NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL, -- MONTHLY, QUARTERLY, ANNUAL
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    auto_renew BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subs_company ON subscriptions (company_id);
CREATE INDEX idx_subs_status ON subscriptions (status);
CREATE INDEX idx_subs_end_date ON subscriptions (end_date);

-- Invoices Table
CREATE TABLE IF NOT EXISTS invoices (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    subscription_id VARCHAR(255),
    invoice_number VARCHAR(100) UNIQUE NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    tax DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    status VARCHAR(50) DEFAULT 'PENDING',
    items JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
);

CREATE INDEX idx_invoices_company ON invoices (company_id);
CREATE INDEX idx_invoices_subscription ON invoices (subscription_id);
CREATE INDEX idx_invoices_status ON invoices (status);
CREATE INDEX idx_invoices_due_date ON invoices (due_date);
CREATE INDEX idx_invoices_number ON invoices (invoice_number);

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(255) PRIMARY KEY,
    invoice_id VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    gateway_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE INDEX idx_payments_invoice ON payments (invoice_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_date ON payments (payment_date);
CREATE INDEX idx_payments_transaction ON payments (transaction_id);

-- Pricing Plans Table
CREATE TABLE IF NOT EXISTS pricing_plans (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price_per_vehicle DECIMAL(10, 2) NOT NULL,
    features JSONB,
    max_vehicles INTEGER,
    billing_cycle VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment Methods Table
CREATE TABLE IF NOT EXISTS payment_methods (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    method_type VARCHAR(50) NOT NULL,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    gateway_customer_id VARCHAR(255),
    gateway_payment_method_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_methods_company ON payment_methods (company_id);
CREATE INDEX idx_payment_methods_default ON payment_methods (is_default);

-- Usage Records Table (for metered billing)
CREATE TABLE IF NOT EXISTS usage_records (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    subscription_id VARCHAR(255) NOT NULL,
    usage_type VARCHAR(100) NOT NULL,
    quantity DECIMAL(12, 4) NOT NULL,
    unit_price DECIMAL(10, 4),
    total_cost DECIMAL(12, 2),
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
);

CREATE INDEX idx_usage_company ON usage_records (company_id);
CREATE INDEX idx_usage_subscription ON usage_records (subscription_id);
CREATE INDEX idx_usage_period ON usage_records (period_start, period_end);

-- Credits Table (for prepaid balance)
CREATE TABLE IF NOT EXISTS credits (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_added DECIMAL(12, 2) DEFAULT 0,
    total_used DECIMAL(12, 2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'INR',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_credits_company ON credits (company_id);

-- Credit Transactions Table
CREATE TABLE IF NOT EXISTS credit_transactions (
    id VARCHAR(255) PRIMARY KEY,
    credit_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- CREDIT, DEBIT
    amount DECIMAL(12, 2) NOT NULL,
    balance_after DECIMAL(12, 2) NOT NULL,
    reference_id VARCHAR(255),
    reference_type VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (credit_id) REFERENCES credits(id)
);

CREATE INDEX idx_credit_trans_credit ON credit_transactions (credit_id);
CREATE INDEX idx_credit_trans_type ON credit_transactions (transaction_type);
CREATE INDEX idx_credit_trans_created ON credit_transactions (created_at);

-- Insert Sample Pricing Plans
INSERT INTO pricing_plans (id, name, description, price_per_vehicle, features, max_vehicles, billing_cycle, is_active)
VALUES
    ('PLAN001', 'Starter', 'Perfect for small fleets', 99.00,
     '{"features": ["Basic Analytics", "5 Vehicles", "Email Support"]}'::jsonb, 5, 'MONTHLY', true),

    ('PLAN002', 'Professional', 'For growing businesses', 199.00,
     '{"features": ["Advanced Analytics", "25 Vehicles", "Priority Support", "Custom Reports"]}'::jsonb, 25, 'MONTHLY', true),

    ('PLAN003', 'Enterprise', 'For large fleets', 499.00,
     '{"features": ["All Features", "Unlimited Vehicles", "24/7 Support", "API Access", "Custom Integrations"]}'::jsonb, NULL, 'MONTHLY', true),

    ('PLAN004', 'Starter Annual', 'Annual plan with discount', 990.00,
     '{"features": ["Basic Analytics", "5 Vehicles", "Email Support"]}'::jsonb, 5, 'ANNUAL', true),

    ('PLAN005', 'Professional Annual', 'Annual plan with discount', 1990.00,
     '{"features": ["Advanced Analytics", "25 Vehicles", "Priority Support", "Custom Reports"]}'::jsonb, 25, 'ANNUAL', true)
ON CONFLICT (id) DO NOTHING;

-- Insert Sample Subscriptions
INSERT INTO subscriptions (id, company_id, plan_type, vehicle_count, amount, billing_cycle, start_date, end_date, status)
VALUES
    ('SUB001', 'COMP001', 'PLAN002', 15, 199.00, 'MONTHLY', CURRENT_DATE - INTERVAL '2 months', CURRENT_DATE + INTERVAL '10 months', 'ACTIVE'),
    ('SUB002', 'COMP002', 'PLAN001', 5, 99.00, 'MONTHLY', CURRENT_DATE - INTERVAL '1 month', CURRENT_DATE + INTERVAL '11 months', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Insert Sample Invoices
INSERT INTO invoices (id, company_id, subscription_id, invoice_number, amount, tax, total_amount, due_date, status, items)
VALUES
    ('INV001', 'COMP001', 'SUB001', 'INV-2025-001', 199.00, 35.82, 234.82, CURRENT_DATE + INTERVAL '15 days', 'PENDING',
     '{"items": [{"description": "Professional Plan - Monthly", "amount": 199.00}]}'::jsonb),

    ('INV002', 'COMP002', 'SUB002', 'INV-2025-002', 99.00, 17.82, 116.82, CURRENT_DATE + INTERVAL '10 days', 'PENDING',
     '{"items": [{"description": "Starter Plan - Monthly", "amount": 99.00}]}'::jsonb),

    ('INV003', 'COMP001', 'SUB001', 'INV-2025-003', 199.00, 35.82, 234.82, CURRENT_DATE - INTERVAL '15 days', 'PAID',
     '{"items": [{"description": "Professional Plan - Monthly", "amount": 199.00}]}'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- Insert Sample Payments
INSERT INTO payments (id, invoice_id, amount, payment_method, transaction_id, payment_date, status, gateway_response)
VALUES
    ('PAY001', 'INV003', 234.82, 'CARD', 'TXN123456789', CURRENT_TIMESTAMP - INTERVAL '10 days', 'SUCCESS',
     '{"gateway": "razorpay", "payment_id": "pay_123456", "status": "captured"}'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- Insert Sample Credits
INSERT INTO credits (id, company_id, balance, total_added, total_used)
VALUES
    ('CRD001', 'COMP001', 5000.00, 10000.00, 5000.00),
    ('CRD002', 'COMP002', 2500.00, 5000.00, 2500.00)
ON CONFLICT (id) DO NOTHING;

-- Function to update invoice status when payment is made
CREATE OR REPLACE FUNCTION update_invoice_on_payment()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'SUCCESS' THEN
        UPDATE invoices
        SET status = 'PAID', paid_date = NEW.payment_date, updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.invoice_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER invoice_payment_trigger
AFTER INSERT OR UPDATE ON payments
FOR EACH ROW EXECUTE FUNCTION update_invoice_on_payment();

-- Update Timestamp Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pricing_plans_updated_at BEFORE UPDATE ON pricing_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
