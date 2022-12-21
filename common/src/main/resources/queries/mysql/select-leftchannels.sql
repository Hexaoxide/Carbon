SELECT channel FROM carbon_leftchannels WHERE (id = UNHEX(REPLACE(:id, '-', '')));
