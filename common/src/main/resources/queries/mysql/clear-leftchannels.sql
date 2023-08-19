DELETE FROM carbon_leftchannels WHERE (id = UNHEX(REPLACE(:id, '-', '')));
