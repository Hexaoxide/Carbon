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
    party,
    applycustomfilters
FROM carbon_users WHERE (id = :id);
