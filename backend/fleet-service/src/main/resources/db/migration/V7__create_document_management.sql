-- Document Management Tables

-- Document Types Enum
CREATE TYPE document_type AS ENUM (
    'VEHICLE_RC',
    'VEHICLE_INSURANCE',
    'VEHICLE_PERMIT',
    'VEHICLE_FITNESS',
    'VEHICLE_PUC',
    'VEHICLE_TAX',
    'DRIVER_LICENSE',
    'DRIVER_BADGE',
    'DRIVER_AADHAR',
    'DRIVER_PAN',
    'DRIVER_PHOTO',
    'OTHER'
);

-- Document Status Enum
CREATE TYPE document_status AS ENUM (
    'ACTIVE',
    'EXPIRED',
    'EXPIRING_SOON',
    'PENDING_RENEWAL',
    'INVALID'
);

-- Documents Table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(100) NOT NULL,
    document_type document_type NOT NULL,
    document_status document_status DEFAULT 'ACTIVE',
    
    -- Ownership
    entity_type VARCHAR(50) NOT NULL, -- 'VEHICLE' or 'DRIVER'
    entity_id BIGINT NOT NULL,
    
    -- Document Details
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    
    -- Dates
    issue_date DATE,
    expiry_date DATE,
    
    -- Issuing Authority
    issuing_authority VARCHAR(255),
    issuing_location VARCHAR(255),
    
    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by VARCHAR(255),
    verified_at TIMESTAMP,
    
    -- Metadata
    tags TEXT[],
    notes TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Constraints
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('VEHICLE', 'DRIVER')),
    CONSTRAINT chk_expiry_after_issue CHECK (expiry_date IS NULL OR expiry_date >= issue_date)
);

-- Document Reminders Table
CREATE TABLE document_reminders (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    
    -- Reminder Details
    reminder_type VARCHAR(50) NOT NULL, -- 'EXPIRY_WARNING', 'RENEWAL_DUE', etc.
    reminder_date DATE NOT NULL,
    days_before_expiry INT,
    
    -- Notification
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    notification_channel VARCHAR(50), -- 'EMAIL', 'SMS', 'IN_APP'
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Document History Table (for audit trail)
CREATE TABLE document_history (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    
    -- Change Details
    action VARCHAR(50) NOT NULL, -- 'CREATED', 'UPDATED', 'DELETED', 'VERIFIED', 'RENEWED'
    old_status document_status,
    new_status document_status,
    changes JSONB,
    
    -- Audit
    changed_by VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Indexes for performance
CREATE INDEX idx_documents_entity ON documents(entity_type, entity_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_status ON documents(document_status);
CREATE INDEX idx_documents_expiry ON documents(expiry_date);
CREATE INDEX idx_documents_created_at ON documents(created_at);

CREATE INDEX idx_document_reminders_document ON document_reminders(document_id);
CREATE INDEX idx_document_reminders_date ON document_reminders(reminder_date);
CREATE INDEX idx_document_reminders_sent ON document_reminders(is_sent);

CREATE INDEX idx_document_history_document ON document_history(document_id);
CREATE INDEX idx_document_history_action ON document_history(action);

-- Function to update document status based on expiry
CREATE OR REPLACE FUNCTION update_document_status()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.expiry_date IS NOT NULL THEN
        IF NEW.expiry_date < CURRENT_DATE THEN
            NEW.document_status = 'EXPIRED';
        ELSIF NEW.expiry_date <= CURRENT_DATE + INTERVAL '30 days' THEN
            NEW.document_status = 'EXPIRING_SOON';
        ELSE
            NEW.document_status = 'ACTIVE';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update document status
CREATE TRIGGER trg_update_document_status
    BEFORE INSERT OR UPDATE ON documents
    FOR EACH ROW
    EXECUTE FUNCTION update_document_status();

-- Function to create automatic reminders
CREATE OR REPLACE FUNCTION create_document_reminders()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.expiry_date IS NOT NULL THEN
        -- Create reminder 30 days before expiry
        INSERT INTO document_reminders (document_id, reminder_type, reminder_date, days_before_expiry)
        VALUES (NEW.id, 'EXPIRY_WARNING_30', NEW.expiry_date - INTERVAL '30 days', 30);
        
        -- Create reminder 15 days before expiry
        INSERT INTO document_reminders (document_id, reminder_type, reminder_date, days_before_expiry)
        VALUES (NEW.id, 'EXPIRY_WARNING_15', NEW.expiry_date - INTERVAL '15 days', 15);
        
        -- Create reminder 7 days before expiry
        INSERT INTO document_reminders (document_id, reminder_type, reminder_date, days_before_expiry)
        VALUES (NEW.id, 'EXPIRY_WARNING_7', NEW.expiry_date - INTERVAL '7 days', 7);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to create automatic reminders on document creation
CREATE TRIGGER trg_create_document_reminders
    AFTER INSERT ON documents
    FOR EACH ROW
    EXECUTE FUNCTION create_document_reminders();
