CREATE TABLE carbon_users (
    `id` UUID NOT NULL PRIMARY KEY,
    `muted` BOOLEAN,
    `deafened` BOOLEAN,
    `selectedchannel` varchar(256),
    `username` varchar(20),
    `displayname` varchar(1024),
    `lastwhispertarget` UUID,
    `whisperreplytarget` UUID,
    `spying` BOOLEAN
);

CREATE TABLE carbon_ignores (
    `id` UUID NOT NULL,
    `ignoredplayer` UUID NOT NULL,
    PRIMARY KEY (id, ignoredplayer)
);
