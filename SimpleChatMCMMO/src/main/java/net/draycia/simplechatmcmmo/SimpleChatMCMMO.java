package net.draycia.simplechatmcmmo;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChatMCMMO extends JavaPlugin {

    private SimpleChat simpleChat;

    @Override
    public void onEnable() {
        simpleChat = (SimpleChat) Bukkit.getPluginManager().getPlugin("SimpleChat");

        simpleChat.getContextManager().register("mcmmo-party", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameParty(context.getSender(), context.getTarget());
            }

            return true;
        });
    }

    private final McMMOPartyChangeEvent.EventReason LEFT = McMMOPartyChangeEvent.EventReason.LEFT_PARTY;
    private final McMMOPartyChangeEvent.EventReason KICKED = McMMOPartyChangeEvent.EventReason.KICKED_FROM_PARTY;

    @EventHandler
    public void onPartyLeave(McMMOPartyChangeEvent event) {
        if (event.getReason() != LEFT && event.getReason() != KICKED) {
            return;
        }

        ChatUser user = simpleChat.getUserService().wrap(event.getPlayer());
        Object party = user.getSelectedChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party)) {
            user.clearSelectedChannel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = simpleChat.getUserService().wrap(event.getPlayer());
        Object party = user.getSelectedChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party) && !isInParty(user)) {
            user.clearSelectedChannel();
        }
    }

    public boolean isInParty(ChatUser user) {
        return PartyAPI.inParty(user.asPlayer());
    }

    public boolean isInSameParty(ChatUser user1, ChatUser user2) {
        if (!user1.isOnline() || !user2.isOnline()) {
            return false;
        }

        return PartyAPI.inSameParty(user1.asPlayer(), user2.asPlayer());
    }

}
