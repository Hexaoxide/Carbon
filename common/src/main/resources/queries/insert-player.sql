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
    party
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
    :party
){PSQL: ON CONFLICT DO NOTHING};
