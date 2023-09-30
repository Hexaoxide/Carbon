CREATE TABLE carbon_party_members (
    `partyid` BINARY(16) NOT NULL,
    `playerid` BINARY(16) NOT NULL,
    PRIMARY KEY (partyid, playerid)
);

CREATE TABLE carbon_parties (
    `partyid` BINARY(16) NOT NULL PRIMARY KEY,
    `name` VARCHAR(256)
);

ALTER TABLE carbon_users ADD COLUMN party BINARY(16);
