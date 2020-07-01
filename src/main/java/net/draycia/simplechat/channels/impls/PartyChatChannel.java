package net.draycia.simplechat.channels.impls;

import com.gmail.nossr50.api.PartyAPI;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;

import java.util.Map;

public class PartyChatChannel extends SimpleChatChannel {

    PartyChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, simpleChat);
    }

    @Override
    public boolean canPlayerSee(ChatUser sender, ChatUser target) {
        if (super.canPlayerSee(sender, target) && sender != null) {
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

    public static PartyChatChannel.Builder partyBuilder(String name) {
        return new PartyChatChannel.Builder(name);
    }

    public static class Builder extends SimpleChatChannel.Builder {
        private Builder(String name) {
            super(name);
        }
    }

}
