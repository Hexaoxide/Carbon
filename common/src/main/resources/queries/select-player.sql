SELECT
    id,
    muted,
    deafened,
    selectedchannel,
    displayname,
    lastwhispertarget,
    whisperreplytarget,
    spying,
    ignoringdms,
    party
FROM carbon_users WHERE (id = :id);
