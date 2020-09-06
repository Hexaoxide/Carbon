package net.draycia.carbon.channels.contexts.impl;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class mcMMOContext implements Listener {

    private final CarbonChat carbonChat;

    public mcMMOContext(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        this.carbonChat.getContextManager().register("mcmmo-party", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameParty(context.getSender(), context.getTarget());
            }

            return true;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onChannelSwitch(@NonNull ChannelSwitchEvent event) {
        Object party = event.getChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party)) {
            if (!isInParty(event.getUser())) {
                event.setCancelled(true);
                event.setFailureMessage(carbonChat.getConfig().getString("contexts.mcMMO.cancellation-message"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChannelMessage(@NonNull PreChatFormatEvent event) {
        Object party = event.getChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party)) {
            if (!isInParty(event.getUser())) {
                event.setCancelled(true);
            }
        }
    }

    private final McMMOPartyChangeEvent.EventReason LEFT = McMMOPartyChangeEvent.EventReason.LEFT_PARTY;
    private final McMMOPartyChangeEvent.EventReason KICKED = McMMOPartyChangeEvent.EventReason.KICKED_FROM_PARTY;

    @EventHandler(ignoreCancelled = true)
    public void onPartyLeave(@NonNull McMMOPartyChangeEvent event) {
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
    public void onPlayerJoin(@NonNull PlayerJoinEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());
        Object party = user.getSelectedChannel().getContext("mcmmo-party");

        if ((party instanceof Boolean) && ((Boolean) party) && !isInParty(user)) {
            user.clearSelectedChannel();
        }
    }

    public boolean isInParty(@NonNull ChatUser user) {
        return PartyAPI.inParty(user.asPlayer());
    }

    public boolean isInSameParty(@NonNull ChatUser user1, @NonNull ChatUser user2) {
        if (!user1.isOnline() || !user2.isOnline()) {
            return false;
        }

        return PartyAPI.inSameParty(user1.asPlayer(), user2.asPlayer());
    }

}
