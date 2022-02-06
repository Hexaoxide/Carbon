UPDATE carbon_users
SET
    id = :id,
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    username= :username,
    displayname = :displayname,
    lastwhispertarget = :lastwhispertarget,
    whisperreplytarget = :whisperreplytarget,
    spying = :spying
WHERE id = :id;
