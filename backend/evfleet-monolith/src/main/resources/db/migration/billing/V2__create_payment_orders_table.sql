-- V2__create_payment_orders_table.sql
-- Razorpay Payment Orders table for tracking payment lifecycle
-- Created: 2025-11-30

CREATE TABLE IF NOT EXISTS payment_orders (
    id BIGSERIAL PRIMARY KEY,
    razorpay_order_id VARCHAR(50) NOT NULL UNIQUE,
    razorpay_payment_id VARCHAR(50) UNIQUE,
    razorpay_refund_id VARCHAR(50),
    invoice_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    amount_paid BIGINT,
    amount_refunded BIGINT DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    receipt VARCHAR(40),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    payment_method VARCHAR(50),
    bank VARCHAR(100),
    wallet VARCHAR(50),
    vpa VARCHAR(100),
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20),
    razorpay_fee BIGINT,
    razorpay_tax BIGINT,
    error_code VARCHAR(100),
    error_description VARCHAR(500),
    error_reason VARCHAR(255),
    notes TEXT,
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    expires_at TIMESTAMP,
    attempts INTEGER DEFAULT 0,
    signature VARCHAR(255),
    signature_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_po_razorpay_order_id ON payment_orders(razorpay_order_id);
CREATE INDEX IF NOT EXISTS idx_po_razorpay_payment_id ON payment_orders(razorpay_payment_id);
CREATE INDEX IF NOT EXISTS idx_po_invoice_id ON payment_orders(invoice_id);
CREATE INDEX IF NOT EXISTS idx_po_company_id ON payment_orders(company_id);
CREATE INDEX IF NOT EXISTS idx_po_status ON payment_orders(status);
CREATE INDEX IF NOT EXISTS idx_po_created_at ON payment_orders(created_at);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_po_company_status ON payment_orders(company_id, status);
CREATE INDEX IF NOT EXISTS idx_po_invoice_status ON payment_orders(invoice_id, status);
CREATE INDEX IF NOT EXISTS idx_po_status_expires ON payment_orders(status, expires_at) WHERE status = 'CREATED';

-- Comments
COMMENT ON TABLE payment_orders IS 'Tracks Razorpay payment orders and their lifecycle';
COMMENT ON COLUMN payment_orders.razorpay_order_id IS 'Razorpay Order ID (order_xxx format)';
COMMENT ON COLUMN payment_orders.razorpay_payment_id IS 'Razorpay Payment ID (pay_xxx format)';
COMMENT ON COLUMN payment_orders.amount IS 'Amount in smallest currency unit (paise for INR)';
COMMENT ON COLUMN payment_orders.status IS 'CREATED, ATTEMPTED, AUTHORIZED, PAID, FAILED, EXPIRED, REFUND_INITIATED, PARTIALLY_REFUNDED, REFUNDED, CANCELLED';
