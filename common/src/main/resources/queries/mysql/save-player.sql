UPDATE carbon_users
SET
    id = UNHEX(REPLACE(:id, '-', '')),
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    username= :username,
    displayname = :displayname,
    lastwhispertarget = UNHEX(REPLACE(:lastwhispertarget, '-', '')),
    whisperreplytarget = UNHEX(REPLACE(:whisperreplytarget, '-', '')),
    spying = :spying
WHERE id = UNHEX(REPLACE(:id, '-', ''));
