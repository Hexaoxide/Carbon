DROP TABLE carbon_leftchannels;
CREATE TABLE carbon_leftchannels (
    `id` BINARY(16) NOT NULL,
    `channel` VARCHAR(256) NOT NULL,
    PRIMARY KEY (id, channel)
);
