package net.draycia.simplechat.channels.impls;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PartyChatChannel extends SimpleChatChannel implements Listener {

    public PartyChatChannel(String name, SimpleChat simpleChat) {
        super(name, simpleChat);
    }

    @Override
    public String processPlaceholders(ChatUser user, String input) {
        String partyName = PartyAPI.getPartyName(user.asPlayer());

        if (partyName == null) {
            partyName = "NotLoaded";
        }

        return input.replace("<party>", partyName);
    }

    @Override
    public boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
        if (checkSpying && target.asPlayer().hasPermission("simplechat.spy." + getName())) {
            if (target.getChannelSettings(this).isSpying()) {
                return true;
            }
        }

        if (super.canPlayerSee(sender, target, false) && sender != null) {
            return PartyAPI.inSameParty(sender.asPlayer(), target.asPlayer());
        }

        return false;
    }

    @Override
    public boolean canPlayerUse(ChatUser user) {
        if (super.canPlayerUse(user)) {
            return PartyAPI.inParty(user.asPlayer());
        }

        return false;
    }

    private McMMOPartyChangeEvent.EventReason LEFT = McMMOPartyChangeEvent.EventReason.LEFT_PARTY;
    private McMMOPartyChangeEvent.EventReason KICKED = McMMOPartyChangeEvent.EventReason.KICKED_FROM_PARTY;

    @EventHandler
    public void onPartyLeave(McMMOPartyChangeEvent event) {
        if (event.getReason() != LEFT && event.getReason() != KICKED) {
            return;
        }

        ChatUser user = getSimpleChat().getUserService().wrap(event.getPlayer());

        if (user.getSelectedChannel().isPartyChat()) {
            user.clearSelectedChannel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = getSimpleChat().getUserService().wrap(event.getPlayer());

        if (user.getSelectedChannel().isPartyChat() && !isInParty(user)) {
            user.clearSelectedChannel();
        }
    }

    public boolean isInParty(ChatUser user) {
        return PartyAPI.inParty(user.asPlayer());
    }

    @Override
    public boolean isPartyChat() {
        return true;
    }

}
