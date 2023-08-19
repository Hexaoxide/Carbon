DELETE FROM carbon_ignores WHERE (id = UNHEX(REPLACE(:id, '-', '')));
