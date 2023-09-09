INSERT{!PSQL: IGNORE} INTO carbon_users(
    id,
    muted,
    deafened,
    selectedchannel,
    displayname,
    lastwhispertarget,
    whisperreplytarget,
    spying,
    ignoringdms
) VALUES (
    :id,
    :muted,
    :deafened,
    :selectedchannel,
    :displayname,
    :lastwhispertarget,
    :whisperreplytarget,
    :spying,
    :ignoringdms
){PSQL: ON CONFLICT DO NOTHING};
