SELECT
    id,
    muted,
    deafened,
    selectedchannel,
    username,
    displayname,
    lastwhispertarget,
    whisperreplytarget,
    spying
FROM carbon_users WHERE (id = :id);
