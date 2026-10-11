BEGIN;

-- A business moment can be created like a group moment, with no company.

ALTER TABLE business.business_moment_context
    ALTER COLUMN company_id DROP NOT NULL;

ALTER TABLE business.business_system_setup
    ALTER COLUMN company_id DROP NOT NULL;

ALTER TABLE business.operating_event
    ALTER COLUMN company_id DROP NOT NULL;

COMMIT;
