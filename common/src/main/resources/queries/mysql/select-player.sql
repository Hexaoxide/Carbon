SELECT
    LOWER(CONCAT(
      LEFT(HEX(id), 8), '-',
      MID(HEX(id), 9, 4), '-',
      MID(HEX(id), 13, 4), '-',
      MID(HEX(id), 17, 4), '-',
      RIGHT(HEX(id), 12)
    )) AS id,
    muted,
    deafened,
    selectedchannel,
    username,
    displayname,
    LOWER(CONCAT(
      LEFT(HEX(lastwhispertarget), 8), '-',
      MID(HEX(lastwhispertarget), 9, 4), '-',
      MID(HEX(lastwhispertarget), 13, 4), '-',
      MID(HEX(lastwhispertarget), 17, 4), '-',
      RIGHT(HEX(lastwhispertarget), 12)
    )) AS lastwhispertarget,
    LOWER(CONCAT(
      LEFT(HEX(whisperreplytarget), 8), '-',
      MID(HEX(whisperreplytarget), 9, 4), '-',
      MID(HEX(whisperreplytarget), 13, 4), '-',
      MID(HEX(whisperreplytarget), 17, 4), '-',
      RIGHT(HEX(whisperreplytarget), 12)
    )) AS whisperreplytarget,
    spying
FROM carbon_users WHERE (id = UNHEX(REPLACE(:id, '-', '')));
