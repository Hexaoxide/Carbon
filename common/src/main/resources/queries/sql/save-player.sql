UPDATE carbon_users
SET
    id = UUID_TO_BIN(:id),
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    username= :username,
    displayname = :displayname,
    lastwhispertarget = UUID_TO_BIN(:lastwhispertarget),
    whisperreplytarget = UUID_TO_BIN(:whisperreplytarget),
    spying = :spying
WHERE id = UUID_TO_BIN(:id);
