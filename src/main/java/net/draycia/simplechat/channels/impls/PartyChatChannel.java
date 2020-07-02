package net.draycia.simplechat.channels.impls;

import com.gmail.nossr50.api.PartyAPI;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;
import java.util.Map;

public class PartyChatChannel extends SimpleChatChannel {

    PartyChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, boolean firstMatchingGroup, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
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

    public static class Builder extends ChatChannel.Builder {

        private TextColor color = TextColor.of(255, 255, 255);
        private long id = -1;
        private Map<String, String> formats = new HashMap<>();
        private String webhook = null;
        private boolean isDefault = false;
        private boolean ignorable = true;
        private String name;
        private double distance = -1;
        private String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";
        private String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";
        private String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";
        private boolean forwardFormatting = true;
        private boolean shouldBungee = false;
        private boolean filterEnabled = true;
        private boolean firstMatchingGroup = false;

        Builder(String name) {
            this.name = name.toLowerCase();
        }

        @Override
        public PartyChatChannel build(SimpleChat simpleChat) {
            return new PartyChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
        }

        @Override
        public PartyChatChannel.Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setColor(String color) {
            return setColor(TextColor.fromHexString(color));
        }

        @Override
        public PartyChatChannel.Builder setId(long id) {
            this.id = id;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setIsDefault(boolean aDefault) {
            this.isDefault = aDefault;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setDistance(double distance) {
            this.distance = distance;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setSwitchMessage(String switchMessage) {
            this.switchMessage = switchMessage;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setToggleOffMessage(String toggleOffMessage) {
            this.toggleOffMessage = toggleOffMessage;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setToggleOnMessage(String toggleOnMessage) {
            this.toggleOnMessage = toggleOnMessage;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setShouldForwardFormatting(boolean forwardFormatting) {
            this.forwardFormatting = forwardFormatting;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setShouldBungee(boolean shouldBungee) {
            this.shouldBungee = shouldBungee;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;

            return this;
        }

        @Override
        public PartyChatChannel.Builder setFirstMatchingGroup(boolean firstMatchingGroup) {
            this.firstMatchingGroup = firstMatchingGroup;

            return this;
        }
    }

}
