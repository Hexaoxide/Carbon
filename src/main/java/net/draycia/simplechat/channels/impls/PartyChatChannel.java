package net.draycia.simplechat.channels.impls;

import com.gmail.nossr50.api.PartyAPI;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;

public class PartyChatChannel extends SimpleChatChannel {

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

    public boolean isInParty(ChatUser user) {
        return PartyAPI.inParty(user.asPlayer());
    }

    @Override
    public boolean isPartyChat() {
        return true;
    }

}
