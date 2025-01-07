INSERT{!PSQL: IGNORE} INTO carbon_users(
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
) VALUES (
    :id,
    :muted,
    :deafened,
    :selectedchannel,
    :displayname,
    :lastwhispertarget,
    :whisperreplytarget,
    :spying,
    :ignoringdms,
    :party,
    :applycustomfilters
){PSQL: ON CONFLICT DO NOTHING};
