ALTER TABLE carbon_leftchannels
    ALTER COLUMN channel TYPE VARCHAR(256),
    ALTER COLUMN channel SET NOT NULL;
