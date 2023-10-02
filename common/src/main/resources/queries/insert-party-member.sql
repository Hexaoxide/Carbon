INSERT{!PSQL: IGNORE} INTO carbon_party_members (partyid, playerid) VALUES(:partyid, :playerid){PSQL: ON CONFLICT DO NOTHING};
