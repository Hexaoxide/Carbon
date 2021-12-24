CREATE TABLE carbon_users (
    `id` BINARY(16) NOT NULL PRIMARY KEY,
    `muted` BOOLEAN,
    `deafened` BOOLEAN,
    `selectedchannel` varchar(256),
    `username` varchar(20),
    `displayname` varchar(1024),
    `lastwhispertarget` BINARY(16),
    `whisperreplytarget` BINARY(16),
    `spying` BOOLEAN
);

CREATE TABLE carbon_ignores (
    `id` BINARY(16) NOT NULL,
    `ignoredplayer` BINARY(16) NOT NULL,
    PRIMARY KEY (id, ignoredplayer)
);
