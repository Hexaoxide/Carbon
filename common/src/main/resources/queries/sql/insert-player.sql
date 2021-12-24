INSERT INTO carbon_users VALUES (
    UUID_TO_BIN(:id),
    :muted,
    :deafened,
    :selectedchannel,
    :username,
    :displayname,
    UUID_TO_BIN(:lastwhispertarget),
    UUID_TO_BIN(:whisperreplytarget),
    :spying
);
