CREATE TABLE carbon_party_members (
    `partyid` UUID NOT NULL,
    `playerid` UUID NOT NULL,
    PRIMARY KEY (partyid, playerid)
);

CREATE TABLE carbon_parties (
    `partyid` UUID NOT NULL PRIMARY KEY,
    `name` VARCHAR(256)
);

ALTER TABLE carbon_users ADD COLUMN party UUID;
