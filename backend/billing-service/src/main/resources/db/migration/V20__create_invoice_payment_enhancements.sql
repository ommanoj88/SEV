-- PR 18: Invoice Generation and Payment Processing
-- Enhancements to invoices and payments tables for multi-fuel billing

-- Add new columns to invoices table for PR 18
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS vehicle_count INTEGER;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS invoice_month VARCHAR(7);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS charges_by_tier JSONB;

-- Add new columns to payments table for PR 18
ALTER TABLE payments ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500);

-- Create index on invoice_month for efficient queries
CREATE INDEX IF NOT EXISTS idx_invoices_month ON invoices(invoice_month);
CREATE INDEX IF NOT EXISTS idx_invoices_vehicle_count ON invoices(vehicle_count);

-- Create index on payment status for tracking
CREATE INDEX IF NOT EXISTS idx_payments_processed_at ON payments(processed_at);
CREATE INDEX IF NOT EXISTS idx_payments_failure_reason ON payments(failure_reason);

-- Add comment on tables for documentation
COMMENT ON TABLE invoices IS 'Invoices table with multi-fuel pricing breakdown by tier. PR 18 adds vehicle_count, invoice_month, and charges_by_tier for monthly billing.';
COMMENT ON TABLE payments IS 'Payments table with enhanced tracking. PR 18 adds processed_at and failure_reason for better payment flow management.';

-- Add comment on new columns
COMMENT ON COLUMN invoices.vehicle_count IS 'Number of vehicles billed in this invoice';
COMMENT ON COLUMN invoices.invoice_month IS 'Month of invoice in format YYYY-MM';
COMMENT ON COLUMN invoices.charges_by_tier IS 'JSON breakdown of charges by pricing tier (BASIC, EV_PREMIUM, ENTERPRISE)';
COMMENT ON COLUMN payments.processed_at IS 'Timestamp when payment was processed';
COMMENT ON COLUMN payments.failure_reason IS 'Reason for payment failure if status is FAILED';

-- Ensure backward compatibility: Set default values for existing records
UPDATE invoices SET vehicle_count = 1 WHERE vehicle_count IS NULL;
UPDATE invoices SET invoice_month = TO_CHAR(created_at, 'YYYY-MM') WHERE invoice_month IS NULL;
UPDATE invoices SET charges_by_tier = '{}' WHERE charges_by_tier IS NULL;
UPDATE payments SET processed_at = created_at WHERE processed_at IS NULL;
