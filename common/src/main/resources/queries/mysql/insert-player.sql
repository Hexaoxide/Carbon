INSERT INTO carbon_users VALUES (
    UNHEX(REPLACE(:id, '-', '')),
    :muted,
    :deafened,
    :selectedchannel,
    :username,
    :displayname,
    UNHEX(REPLACE(:lastwhispertarget, '-', '')),
    UNHEX(REPLACE(:whisperreplytarget, '-', '')),
    :spying
);
