package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;
import java.util.Map;

public class NationChatChannel extends SimpleChatChannel {

    NationChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, boolean firstMatchingGroup, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
    }

    @Override
    public boolean canPlayerSee(ChatUser sender, ChatUser target) {
        if (super.canPlayerSee(sender, target) && sender != null) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(sender.asPlayer().getName());

                if (resident.hasNation()) {
                    Nation nation = resident.getTown().getNation();

                    if (nation.hasResident(target.asPlayer().getName())) {
                        return true;
                    }
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
                return TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName()).hasNation();
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static NationChatChannel.Builder nationBuilder(String name) {
        return new NationChatChannel.Builder(name);
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
        public NationChatChannel build(SimpleChat simpleChat) {
            return new NationChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
        }

        @Override
        public NationChatChannel.Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        @Override
        public NationChatChannel.Builder setColor(String color) {
            return setColor(TextColor.fromHexString(color));
        }

        @Override
        public NationChatChannel.Builder setId(long id) {
            this.id = id;

            return this;
        }

        @Override
        public NationChatChannel.Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

            return this;
        }

        @Override
        public NationChatChannel.Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        @Override
        public NationChatChannel.Builder setIsDefault(boolean aDefault) {
            this.isDefault = aDefault;

            return this;
        }

        @Override
        public NationChatChannel.Builder setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;

            return this;
        }

        @Override
        public NationChatChannel.Builder setName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public NationChatChannel.Builder setDistance(double distance) {
            this.distance = distance;

            return this;
        }

        @Override
        public NationChatChannel.Builder setSwitchMessage(String switchMessage) {
            this.switchMessage = switchMessage;

            return this;
        }

        @Override
        public NationChatChannel.Builder setToggleOffMessage(String toggleOffMessage) {
            this.toggleOffMessage = toggleOffMessage;

            return this;
        }

        @Override
        public NationChatChannel.Builder setToggleOnMessage(String toggleOnMessage) {
            this.toggleOnMessage = toggleOnMessage;

            return this;
        }

        @Override
        public NationChatChannel.Builder setShouldForwardFormatting(boolean forwardFormatting) {
            this.forwardFormatting = forwardFormatting;

            return this;
        }

        @Override
        public NationChatChannel.Builder setShouldBungee(boolean shouldBungee) {
            this.shouldBungee = shouldBungee;

            return this;
        }

        @Override
        public NationChatChannel.Builder setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;

            return this;
        }

        @Override
        public NationChatChannel.Builder setFirstMatchingGroup(boolean firstMatchingGroup) {
            this.firstMatchingGroup = firstMatchingGroup;

            return this;
        }
    }

}
