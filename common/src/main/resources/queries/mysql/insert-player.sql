INSERT INTO carbon_users SET
    id = UNHEX(REPLACE(:id, '-', '')),
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    displayname = :displayname,
    lastwhispertarget = UNHEX(REPLACE(:lastwhispertarget, '-', '')),
    whisperreplytarget = UNHEX(REPLACE(:whisperreplytarget, '-', '')),
    spying = :spying
ON DUPLICATE KEY UPDATE
    id = UNHEX(REPLACE(:id, '-', '')),
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    displayname = :displayname,
    lastwhispertarget = UNHEX(REPLACE(:lastwhispertarget, '-', '')),
    whisperreplytarget = UNHEX(REPLACE(:whisperreplytarget, '-', '')),
    spying = :spying
;
