SELECT
    LOWER(CONCAT(
        LEFT(HEX(ignoredplayer), 8), '-',
        MID(HEX(ignoredplayer), 9, 4), '-',
        MID(HEX(ignoredplayer), 13, 4), '-',
        MID(HEX(ignoredplayer), 17, 4), '-',
        RIGHT(HEX(ignoredplayer), 12)
        )) AS ignoredplayer
FROM carbon_ignores WHERE (id = UNHEX(REPLACE(:id, '-', '')));
