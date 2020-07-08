package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;
import java.util.Map;

public class AllianceChatChannel extends SimpleChatChannel {

    AllianceChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, boolean firstMatchingGroup, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
    }

    @Override
    public boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
        if (checkSpying && target.asPlayer().hasPermission("simplechat.spy." + getName())) {
            if (target.getChannelSettings(this).isSpying()) {
                return true;
            }
        }

        if (super.canPlayerSee(sender, target, false) && sender != null) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(target.asPlayer().getName());
                Resident targetResident = TownyAPI.getInstance().getDataSource().getResident(sender.asPlayer().getName());

                if (resident.isAlliedWith(targetResident)) {
                    return true;
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean canPlayerUse(ChatUser user) {
        if (super.canPlayerUse(user)) {
            try {
                return TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName()).hasTown();
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean isAllianceChat() {
        return true;
    }

    public static AllianceChatChannel.Builder allianceBuilder(String name) {
        return new AllianceChatChannel.Builder(name);
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
        public AllianceChatChannel build(SimpleChat simpleChat) {
            return new AllianceChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
        }

        @Override
        public AllianceChatChannel.Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setColor(String color) {
            return setColor(TextColor.fromHexString(color));
        }

        @Override
        public AllianceChatChannel.Builder setId(long id) {
            this.id = id;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setIsDefault(boolean aDefault) {
            this.isDefault = aDefault;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setDistance(double distance) {
            this.distance = distance;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setSwitchMessage(String switchMessage) {
            this.switchMessage = switchMessage;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setToggleOffMessage(String toggleOffMessage) {
            this.toggleOffMessage = toggleOffMessage;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setToggleOnMessage(String toggleOnMessage) {
            this.toggleOnMessage = toggleOnMessage;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setShouldForwardFormatting(boolean forwardFormatting) {
            this.forwardFormatting = forwardFormatting;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setShouldBungee(boolean shouldBungee) {
            this.shouldBungee = shouldBungee;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;

            return this;
        }

        @Override
        public AllianceChatChannel.Builder setFirstMatchingGroup(boolean firstMatchingGroup) {
            this.firstMatchingGroup = firstMatchingGroup;

            return this;
        }
    }

}
