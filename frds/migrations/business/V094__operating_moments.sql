BEGIN;

-- Four operating Business Moments replace Team / Runway / Daily as the create path.
-- Legacy family codes stay valid so existing rows still load.

ALTER TABLE business.business_moment_context
    DROP CONSTRAINT ck_business_moment_context__family;

ALTER TABLE business.business_moment_context
    ADD CONSTRAINT ck_business_moment_context__family CHECK (
        business_family IN (
            'TEAM_OPERATIONS',
            'BUSINESS_RUNWAY',
            'BUSINESS_OPERATIONS',
            'EVENTS_OPERATIONS',
            'VENDOR_OPERATIONS',
            'MARKETING_CAMPAIGNS',
            'TRADING_DISTRIBUTION',
            'RETAIL_CONSUMER',
            'MANUFACTURING_PRODUCTION'
        )
    );

ALTER TABLE business.business_system_setup
    DROP CONSTRAINT ck_business_system_setup__family;

ALTER TABLE business.business_system_setup
    ADD CONSTRAINT ck_business_system_setup__family CHECK (
        family_code IN (
            'TEAM_OPERATIONS',
            'BUSINESS_RUNWAY',
            'BUSINESS_OPERATIONS',
            'MARKETING_CAMPAIGNS',
            'TRADING_DISTRIBUTION',
            'RETAIL_CONSUMER',
            'MANUFACTURING_PRODUCTION'
        )
    );

INSERT INTO core.moment_category (moment_category_id, domain_code, code, display_name, description, sort_order, status)
VALUES
    ('ea4d2b99-5c53-58f6-a4c4-d1c98cc22deb'::uuid, 'BUSINESS', 'MARKETING_CAMPAIGNS', 'Marketing & Campaigns', 'Client and own-business campaign moments.', 60, 'ACTIVE'),
    ('f9b67602-d5b0-5585-93ce-9d7c91d2e1bd'::uuid, 'BUSINESS', 'TRADING_DISTRIBUTION', 'Trading & Distribution', 'Wholesale and distribution moments.', 70, 'ACTIVE'),
    ('33c91347-669d-554d-85e0-ca9eb345eeab'::uuid, 'BUSINESS', 'RETAIL_CONSUMER', 'Retail & Consumer', 'Store, restaurant, and consumer-service moments.', 80, 'ACTIVE'),
    ('4007a465-5e74-5a10-9da9-bb1329715dc0'::uuid, 'BUSINESS', 'MANUFACTURING_PRODUCTION', 'Manufacturing & Production', 'Standard and made-to-order production moments.', 90, 'ACTIVE')
ON CONFLICT (moment_category_id) DO NOTHING;

INSERT INTO core.moment_type (moment_type_id, moment_category_id, domain_code, code, display_name, description, sort_order, status)
VALUES
    ('5f4f0dde-3489-54be-a63a-980fdad84ccd'::uuid, 'ea4d2b99-5c53-58f6-a4c4-d1c98cc22deb'::uuid, 'BUSINESS', 'CLIENT_CAMPAIGN', 'Client Campaign', 'Campaign run for a client.', 10, 'ACTIVE'),
    ('fdb898eb-5aba-599a-a63b-9e4a7f24ca2e'::uuid, 'ea4d2b99-5c53-58f6-a4c4-d1c98cc22deb'::uuid, 'BUSINESS', 'OWN_BUSINESS_CAMPAIGN', 'Own Business Campaign', 'Campaign run for the business itself.', 20, 'ACTIVE'),
    ('c28e7ce5-d42c-59fb-8fdb-0685a392e86c'::uuid, 'f9b67602-d5b0-5585-93ce-9d7c91d2e1bd'::uuid, 'BUSINESS', 'TRADING_WHOLESALE', 'Trading / Wholesale', 'Source, resell, and collect.', 10, 'ACTIVE'),
    ('48846d71-9a32-5aff-9c0d-5f507f121b02'::uuid, 'f9b67602-d5b0-5585-93ce-9d7c91d2e1bd'::uuid, 'BUSINESS', 'DISTRIBUTION_DEALERSHIP', 'Distribution / Dealership', 'Move goods through dealers and territories.', 20, 'ACTIVE'),
    ('0975955b-d6e6-5b3f-9818-9749128bfc3c'::uuid, '33c91347-669d-554d-85e0-ca9eb345eeab'::uuid, 'BUSINESS', 'RETAIL_STORE', 'Retail Store', 'Stock or service sold to consumers.', 10, 'ACTIVE'),
    ('f0f47422-00c2-5c74-a93f-f2ffede9e1c5'::uuid, '33c91347-669d-554d-85e0-ca9eb345eeab'::uuid, 'BUSINESS', 'RESTAURANT_FOOD', 'Restaurant / Food', 'Food service operating moment.', 20, 'ACTIVE'),
    ('b5f725f7-87c6-5060-8a2a-6d05eca32a3c'::uuid, '33c91347-669d-554d-85e0-ca9eb345eeab'::uuid, 'BUSINESS', 'CONSUMER_SERVICE', 'Consumer Service', 'Appointment and job service moment.', 30, 'ACTIVE'),
    ('5fd36f04-eed8-5c41-8a18-a4b9a46e0d00'::uuid, '4007a465-5e74-5a10-9da9-bb1329715dc0'::uuid, 'BUSINESS', 'STANDARD_PRODUCTION', 'Standard Production', 'Repeat production and dispatch.', 10, 'ACTIVE'),
    ('72fe44d1-c5cc-58b6-b57e-9a7e62bb44e1'::uuid, '4007a465-5e74-5a10-9da9-bb1329715dc0'::uuid, 'BUSINESS', 'MADE_TO_ORDER', 'Made-to-Order', 'Job work produced against a customer order.', 20, 'ACTIVE')
ON CONFLICT (moment_type_id) DO NOTHING;

INSERT INTO core.moment_type_capability (
    moment_type_capability_id,
    moment_type_id,
    capability_id,
    is_default,
    sort_order,
    status
)
SELECT
    uuid_generate_v5(
        'a8f3c2e1-4b5d-5a6f-9c0e-1d2e3f4a5b6c'::uuid,
        'MOMENT_CREATE:' || mt.moment_type_id::text
    ),
    mt.moment_type_id,
    c.capability_id,
    true,
    5,
    'ACTIVE'
FROM core.moment_type mt
CROSS JOIN core.capability c
WHERE mt.code IN (
    'CLIENT_CAMPAIGN',
    'OWN_BUSINESS_CAMPAIGN',
    'TRADING_WHOLESALE',
    'DISTRIBUTION_DEALERSHIP',
    'RETAIL_STORE',
    'RESTAURANT_FOOD',
    'CONSUMER_SERVICE',
    'STANDARD_PRODUCTION',
    'MADE_TO_ORDER'
)
  AND c.code = 'MOMENT_CREATE'
ON CONFLICT (moment_type_capability_id) DO NOTHING;

CREATE TABLE business.operating_event (
    operating_event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    moment_id UUID NOT NULL,
    company_id UUID NOT NULL,
    actor_user_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    title TEXT NOT NULL,
    note TEXT,
    amount NUMERIC(18, 4),
    currency_code TEXT,
    counterparty_name TEXT,
    channel_name TEXT,
    payment_relevance TEXT NOT NULL DEFAULT 'NONE',
    payment_status TEXT NOT NULL DEFAULT 'NONE',
    approval_status TEXT NOT NULL DEFAULT 'NONE',
    submitted_by_user_id UUID NOT NULL,
    decided_by_user_id UUID,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_operating_event__moment
        FOREIGN KEY (moment_id) REFERENCES core.moment(moment_id),
    CONSTRAINT fk_operating_event__company
        FOREIGN KEY (company_id) REFERENCES business.company(company_id),
    CONSTRAINT fk_operating_event__actor
        FOREIGN KEY (actor_user_id) REFERENCES core.user_profile(user_id),
    CONSTRAINT ck_operating_event__payment_relevance CHECK (
        payment_relevance IN ('NONE', 'PAYABLE', 'RECEIVABLE', 'REIMBURSEMENT', 'SETTLEMENT')
    ),
    CONSTRAINT ck_operating_event__payment_status CHECK (
        payment_status IN ('NONE', 'PENDING', 'READY', 'PAID', 'COLLECTED', 'SETTLED', 'CANCELLED')
    ),
    CONSTRAINT ck_operating_event__approval_status CHECK (
        approval_status IN ('NONE', 'PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_operating_event__version CHECK (version > 0)
);

CREATE INDEX ix_operating_event__moment_time
    ON business.operating_event (moment_id, occurred_at DESC);

ALTER TABLE business.operating_event ENABLE ROW LEVEL SECURITY;

CREATE POLICY rls_operating_event__select_member ON business.operating_event
FOR SELECT USING (
    security.is_active_company_member(company_id)
    OR security.is_backend_app()
    OR security.is_analytics_worker()
    OR security.is_memory_worker()
    OR security.is_projection_worker()
);

CREATE POLICY rls_operating_event__backend_write ON business.operating_event
FOR ALL USING (security.is_backend_app()) WITH CHECK (security.is_backend_app());

GRANT SELECT, INSERT, UPDATE, DELETE ON business.operating_event TO momentra_app;
GRANT SELECT ON business.operating_event TO momentra_analytics_worker, momentra_projection_worker;

COMMENT ON TABLE business.operating_event IS
    'Quick Add events for Marketing, Trading, Retail, and Manufacturing moments.';

COMMIT;
