ALTER TABLE carbon_users ALTER COLUMN muteexpiration TYPE BIGINT USING (CASE WHEN muteexpiration THEN 1 ELSE 0 END);
