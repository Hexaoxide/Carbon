INSERT{!PSQL: IGNORE} INTO carbon_ignores (id, ignoredplayer) VALUES(:id, :ignoredplayer){PSQL: ON CONFLICT DO NOTHING};
