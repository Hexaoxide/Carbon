CREATE TABLE IF NOT EXISTS carbon_leftchannels (
    id uuid NOT NULL,
    channel varchar(100) NOT NULL,
    PRIMARY KEY (id, channel)
);
