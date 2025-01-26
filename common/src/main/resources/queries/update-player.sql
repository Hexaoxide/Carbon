UPDATE carbon_users SET
    muted = :muted,
    muteexpiration = :muteexpiration,
    deafened = :deafened,
    selectedchannel = :selectedchannel,
    displayname = :displayname,
    lastwhispertarget = :lastwhispertarget,
    whisperreplytarget = :whisperreplytarget,
    spying = :spying,
    ignoringdms = :ignoringdms,
    party = :party,
    applycustomfilters = :applycustomfilters
WHERE (id = :id);
