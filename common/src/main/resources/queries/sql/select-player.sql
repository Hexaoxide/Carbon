SELECT
    BIN_TO_UUID(id) id,
    muted,
    deafened,
    selectedchannel,
    username,
    displayname,
    UUID_TO_BIN(lastwhispertarget) lastwhispertarget,
    UUID_TO_BIN(whisperreplytarget) whisperreplytarget,
    spying
from carbon_users WHERE (id = UUID_TO_BIN(:id));
