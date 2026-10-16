BEGIN;

-- A payment, collection, shipment, or delivery names the record it reduces.
-- company_id stays nullable; these columns do not open khata or finance.expense.

ALTER TABLE business.operating_event
    ADD COLUMN parent_event_id UUID,
    ADD COLUMN quantity NUMERIC(18, 4);

ALTER TABLE business.operating_event
    ADD CONSTRAINT fk_operating_event__parent
        FOREIGN KEY (parent_event_id) REFERENCES business.operating_event (operating_event_id);

ALTER TABLE business.operating_event
    ADD CONSTRAINT ck_operating_event__not_self_parent
        CHECK (parent_event_id IS NULL OR parent_event_id <> operating_event_id);

ALTER TABLE business.operating_event
    ADD CONSTRAINT ck_operating_event__quantity
        CHECK (quantity IS NULL OR quantity >= 0);

CREATE INDEX ix_operating_event__parent
    ON business.operating_event (parent_event_id)
    WHERE parent_event_id IS NOT NULL;

CREATE OR REPLACE FUNCTION business.operating_event_parent_same_moment()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    parent_moment uuid;
BEGIN
    IF NEW.parent_event_id IS NULL THEN
        RETURN NEW;
    END IF;
    SELECT moment_id INTO parent_moment
    FROM business.operating_event
    WHERE operating_event_id = NEW.parent_event_id;
    IF parent_moment IS NULL OR parent_moment <> NEW.moment_id THEN
        RAISE EXCEPTION 'operating event parent must belong to the same moment';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_operating_event__parent_same_moment ON business.operating_event;

CREATE TRIGGER trg_operating_event__parent_same_moment
    BEFORE INSERT OR UPDATE OF parent_event_id, moment_id
    ON business.operating_event
    FOR EACH ROW
    EXECUTE FUNCTION business.operating_event_parent_same_moment();

COMMENT ON COLUMN business.operating_event.parent_event_id IS
    'Same-moment record this event reduces. A payment names a purchase, a collection names a sale, and a delivery names an order.';

COMMENT ON COLUMN business.operating_event.quantity IS
    'Units for an order, shipment, delivery, or production record.';

COMMIT;
