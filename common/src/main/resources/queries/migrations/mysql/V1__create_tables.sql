CREATE TABLE carbon_users (
    `id` BINARY(16) NOT NULL PRIMARY KEY,
    `muted` BOOLEAN,
    `deafened` BOOLEAN,
    `selectedchannel` VARCHAR(256),
    `username` VARCHAR(20),
    `displayname` VARCHAR(1024),
    `lastwhispertarget` BINARY(16),
    `whisperreplytarget` BINARY(16),
    `spying` BOOLEAN
);

CREATE TABLE carbon_ignores (
    `id` BINARY(16) NOT NULL,
    `ignoredplayer` BINARY(16) NOT NULL,
    PRIMARY KEY (id, ignoredplayer)
);
