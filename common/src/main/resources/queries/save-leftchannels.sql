INSERT{!PSQL: IGNORE} INTO carbon_leftchannels (id, channel) VALUES(:id, :channel){PSQL: ON CONFLICT DO NOTHING};
