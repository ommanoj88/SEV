-- Expense Management Tables

-- Expense Category Enum
CREATE TYPE expense_category AS ENUM (
    'FUEL',
    'CHARGING',
    'MAINTENANCE',
    'REPAIRS',
    'PARTS',
    'TOLLS',
    'PARKING',
    'INSURANCE',
    'TAXES',
    'DRIVER_WAGES',
    'DRIVER_ALLOWANCE',
    'FINES',
    'PERMITS',
    'CLEANING',
    'OTHER'
);

-- Expense Status Enum
CREATE TYPE expense_status AS ENUM (
    'DRAFT',
    'PENDING_APPROVAL',
    'APPROVED',
    'REJECTED',
    'PAID',
    'CANCELLED'
);

-- Expenses Table
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    
    -- Reference
    expense_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- Associated Entity
    entity_type VARCHAR(50) NOT NULL, -- 'VEHICLE', 'DRIVER', 'TRIP', 'FLEET'
    entity_id BIGINT NOT NULL,
    vehicle_id BIGINT,
    driver_id BIGINT,
    trip_id BIGINT,
    
    -- Expense Details
    category expense_category NOT NULL,
    subcategory VARCHAR(100),
    description TEXT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    
    -- Date Information
    expense_date DATE NOT NULL,
    
    -- Vendor/Payee
    vendor_name VARCHAR(255),
    vendor_contact VARCHAR(100),
    
    -- Receipt Information
    receipt_number VARCHAR(100),
    receipt_file_path VARCHAR(500),
    receipt_file_name VARCHAR(255),
    
    -- Payment Information
    payment_method VARCHAR(50), -- 'CASH', 'CARD', 'UPI', 'BANK_TRANSFER', etc.
    payment_reference VARCHAR(100),
    payment_date DATE,
    
    -- Approval Workflow
    status expense_status DEFAULT 'DRAFT',
    submitted_by VARCHAR(255),
    submitted_at TIMESTAMP,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    
    -- Reimbursement
    is_reimbursable BOOLEAN DEFAULT FALSE,
    reimbursed BOOLEAN DEFAULT FALSE,
    reimbursement_date DATE,
    
    -- Additional Info
    odometer_reading INT,
    location VARCHAR(255),
    notes TEXT,
    tags TEXT[],
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('VEHICLE', 'DRIVER', 'TRIP', 'FLEET'))
);

-- Expense Approval History Table
CREATE TABLE expense_approval_history (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    
    -- Approval Details
    action VARCHAR(50) NOT NULL, -- 'SUBMITTED', 'APPROVED', 'REJECTED', 'PAID', 'CANCELLED'
    previous_status expense_status,
    new_status expense_status NOT NULL,
    
    -- Actor
    actor VARCHAR(255) NOT NULL,
    actor_role VARCHAR(100),
    
    -- Details
    comments TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Expense Budget Table
CREATE TABLE expense_budgets (
    id BIGSERIAL PRIMARY KEY,
    
    -- Budget Period
    budget_name VARCHAR(255) NOT NULL,
    period_type VARCHAR(50) NOT NULL, -- 'MONTHLY', 'QUARTERLY', 'YEARLY'
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    
    -- Budget Amount
    category expense_category,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    budget_amount DECIMAL(12, 2) NOT NULL,
    
    -- Tracking
    spent_amount DECIMAL(12, 2) DEFAULT 0,
    remaining_amount DECIMAL(12, 2),
    
    -- Alerts
    alert_threshold_percentage INT DEFAULT 80, -- Alert when 80% spent
    alert_sent BOOLEAN DEFAULT FALSE,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    
    CONSTRAINT chk_budget_amount_positive CHECK (budget_amount > 0),
    CONSTRAINT chk_period_dates CHECK (end_date > start_date),
    CONSTRAINT chk_threshold CHECK (alert_threshold_percentage BETWEEN 1 AND 100)
);

-- Indexes for performance
CREATE INDEX idx_expenses_entity ON expenses(entity_type, entity_id);
CREATE INDEX idx_expenses_vehicle ON expenses(vehicle_id);
CREATE INDEX idx_expenses_driver ON expenses(driver_id);
CREATE INDEX idx_expenses_trip ON expenses(trip_id);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_expenses_status ON expenses(status);
CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_created_at ON expenses(created_at);
CREATE INDEX idx_expenses_submitted_by ON expenses(submitted_by);

CREATE INDEX idx_expense_approval_history_expense ON expense_approval_history(expense_id);
CREATE INDEX idx_expense_approval_history_action ON expense_approval_history(action);

CREATE INDEX idx_expense_budgets_period ON expense_budgets(start_date, end_date);
CREATE INDEX idx_expense_budgets_category ON expense_budgets(category);
CREATE INDEX idx_expense_budgets_entity ON expense_budgets(entity_type, entity_id);
CREATE INDEX idx_expense_budgets_active ON expense_budgets(is_active);

-- Function to update budget spent amount
CREATE OR REPLACE FUNCTION update_budget_spent_amount()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPROVED' AND (OLD.status IS NULL OR OLD.status != 'APPROVED') THEN
        -- Find matching budgets and update spent amount
        UPDATE expense_budgets
        SET spent_amount = spent_amount + NEW.amount,
            remaining_amount = budget_amount - (spent_amount + NEW.amount),
            updated_at = CURRENT_TIMESTAMP
        WHERE is_active = TRUE
          AND start_date <= NEW.expense_date
          AND end_date >= NEW.expense_date
          AND (category = NEW.category OR category IS NULL)
          AND (entity_type = NEW.entity_type OR entity_type IS NULL)
          AND (entity_id = NEW.entity_id OR entity_id IS NULL);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update budget on expense approval
CREATE TRIGGER trg_update_budget_spent
    AFTER INSERT OR UPDATE ON expenses
    FOR EACH ROW
    EXECUTE FUNCTION update_budget_spent_amount();

-- Function to generate expense number
CREATE OR REPLACE FUNCTION generate_expense_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.expense_number = 'EXP-' || TO_CHAR(NEW.expense_date, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 6, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to generate expense number
CREATE TRIGGER trg_generate_expense_number
    BEFORE INSERT ON expenses
    FOR EACH ROW
    EXECUTE FUNCTION generate_expense_number();
