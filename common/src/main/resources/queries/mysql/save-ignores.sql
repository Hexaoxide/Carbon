INSERT IGNORE INTO carbon_ignores (id, ignoredplayer) VALUES(UNHEX(REPLACE(:id, '-', '')), UNHEX(REPLACE(:ignoredplayer, '-', '')))
