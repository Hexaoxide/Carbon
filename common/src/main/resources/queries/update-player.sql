UPDATE carbon_users SET
    muted = :muted,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    displayname = :displayname,
    lastwhispertarget = :lastwhispertarget,
    whisperreplytarget = :whisperreplytarget,
    spying = :spying,
    ignoringdms = :ignoringdms,
    party = :party
WHERE (id = :id);
