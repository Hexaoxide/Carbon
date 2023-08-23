CREATE TABLE carbon_users (
    id UUID NOT NULL PRIMARY KEY,
    muted BOOLEAN,
    deafened BOOLEAN,
    selectedchannel VARCHAR(256),
    username VARCHAR(20),
    displayname VARCHAR(1024),
    lastwhispertarget UUID,
    whisperreplytarget UUID,
    spying BOOLEAN
);

CREATE TABLE carbon_ignores (
    id UUID NOT NULL,
    ignoredplayer UUID NOT NULL,
    PRIMARY KEY (id, ignoredplayer)
);
