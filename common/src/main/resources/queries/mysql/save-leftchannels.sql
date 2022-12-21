INSERT IGNORE INTO carbon_leftchannels (id, channel) VALUES(UNHEX(REPLACE(:id, '-', '')), :channel)
