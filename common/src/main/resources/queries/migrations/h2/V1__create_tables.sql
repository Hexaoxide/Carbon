CREATE TABLE carbon_users (
    `id` UUID NOT NULL PRIMARY KEY,
    `muted` BOOLEAN,
    `deafened` BOOLEAN,
    `selectedchannel` VARCHAR(256),
    `displayname` VARCHAR(1024),
    `lastwhispertarget` UUID,
    `whisperreplytarget` UUID,
    `spying` BOOLEAN,
    `ignoringdms` BOOLEAN
);

CREATE TABLE carbon_ignores (
    `id` UUID NOT NULL,
    `ignoredplayer` UUID NOT NULL,
    PRIMARY KEY (id, ignoredplayer)
);

CREATE TABLE carbon_leftchannels (
    `id` UUID NOT NULL,
    `channel` VARCHAR(256) NOT NULL,
    PRIMARY KEY (id, channel)
);
