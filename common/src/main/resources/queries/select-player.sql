SELECT
    id,
    muted,
    deafened,
    selectedchannel,
    displayname,
    lastwhispertarget,
    whisperreplytarget,
    spying,
    ignoringdirectmessages
FROM carbon_users WHERE (id = :id);
