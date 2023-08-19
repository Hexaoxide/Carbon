CREATE TABLE carbon_users (
    id uuid NOT NULL PRIMARY KEY,
    muted BOOLEAN,
    deafened BOOLEAN,
    selectedchannel VARCHAR(256),
    username VARCHAR(20),
    displayname VARCHAR(1024),
    lastwhispertarget uuid,
    whisperreplytarget uuid,
    spying BOOLEAN
);

CREATE TABLE carbon_ignores (
    id uuid NOT NULL,
    ignoredplayer uuid NOT NULL,
    PRIMARY KEY (id, ignoredplayer)
);
