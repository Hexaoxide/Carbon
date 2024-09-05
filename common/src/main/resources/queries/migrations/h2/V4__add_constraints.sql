ALTER TABLE carbon_ignores
    ADD CONSTRAINT carbon_ignores_owner
        FOREIGN KEY (id) REFERENCES carbon_users (id) ON DELETE CASCADE;
ALTER TABLE carbon_ignores
    ADD CONSTRAINT carbon_ignores_ignored
        FOREIGN KEY (ignoredplayer) REFERENCES carbon_users (id) ON DELETE CASCADE;

ALTER TABLE carbon_leftchannels
    ADD CONSTRAINT carbon_leftchannels_owner
        FOREIGN KEY (id) REFERENCES carbon_users (id) ON DELETE CASCADE;

ALTER TABLE carbon_party_members
    ADD CONSTRAINT carbon_party_members_member
        FOREIGN KEY (playerid) REFERENCES carbon_users (id) ON DELETE CASCADE;
ALTER TABLE carbon_party_members
    ADD CONSTRAINT carbon_party_members_party
        FOREIGN KEY (partyid) REFERENCES carbon_parties (partyid) ON DELETE CASCADE;

ALTER TABLE carbon_users
    ADD CONSTRAINT carbon_users_party
        FOREIGN KEY (party) REFERENCES carbon_parties (partyid) ON DELETE SET NULL;
