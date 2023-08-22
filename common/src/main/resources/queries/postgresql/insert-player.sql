INSERT INTO carbon_users SET
    id = :id,
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    displayname = :displayname,
    lastwhispertarget = :lastwhispertarget,
    whisperreplytarget = :whisperreplytarget,
    spying = :spying
ON CONFLICT (id) DO UPDATE SET
    id = :id,
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    displayname = :displayname,
    lastwhispertarget = :lastwhispertarget,
    whisperreplytarget = :whisperreplytarget,
    spying = :spying
;
