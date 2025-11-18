-- Migration V3: Add firstName and lastName columns (if not already present)
-- This migration ensures the firstName and lastName columns exist in the users table

-- Add first_name column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'first_name'
    ) THEN
        ALTER TABLE users ADD COLUMN first_name VARCHAR(50);
        COMMENT ON COLUMN users.first_name IS 'User first name (NEW - Nov 2025)';
    END IF;
END $$;

-- Add last_name column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'last_name'
    ) THEN
        ALTER TABLE users ADD COLUMN last_name VARCHAR(50);
        COMMENT ON COLUMN users.last_name IS 'User last name (NEW - Nov 2025)';
    END IF;
END $$;
