SELECT ignoredplayer FROM carbon_ignores WHERE (id = UNHEX(REPLACE(:id, '-', '')));
