BEGIN;

-- Participants belong to any moment. A retail store has no group moment row,
-- so the old group-only link rejected the owner insert.

ALTER TABLE collaboration.moment_participant
    DROP CONSTRAINT fk_moment_participant__group_moment;

ALTER TABLE collaboration.moment_participant
    ADD CONSTRAINT fk_moment_participant__moment
        FOREIGN KEY (moment_id)
        REFERENCES core.moment(moment_id)
        ON DELETE RESTRICT;

COMMIT;
