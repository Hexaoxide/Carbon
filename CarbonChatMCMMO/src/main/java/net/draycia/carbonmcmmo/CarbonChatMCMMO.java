package net.draycia.carbonmcmmo;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CarbonChatMCMMO extends JavaPlugin implements Listener {

    private CarbonChat carbonChat;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

        carbonChat.getContextManager().register("mcmmo-party", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameParty(context.getSender(), context.getTarget());
            }

            return true;
        });

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChannelSwitch(ChannelSwitchEvent event) {
        Object party = event.getChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party)) {
            if (!isInParty(event.getUser())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChannelMessage(PreChatFormatEvent event) {
        Object party = event.getChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party)) {
            if (!isInParty(event.getUser())) {
                event.setCancelled(true);
            }
        }
    }

    private final McMMOPartyChangeEvent.EventReason LEFT = McMMOPartyChangeEvent.EventReason.LEFT_PARTY;
    private final McMMOPartyChangeEvent.EventReason KICKED = McMMOPartyChangeEvent.EventReason.KICKED_FROM_PARTY;

    @EventHandler
    public void onPartyLeave(McMMOPartyChangeEvent event) {
        if (event.getReason() != LEFT && event.getReason() != KICKED) {
            return;
        }

        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());
        Object party = user.getSelectedChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party)) {
            user.clearSelectedChannel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());
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
