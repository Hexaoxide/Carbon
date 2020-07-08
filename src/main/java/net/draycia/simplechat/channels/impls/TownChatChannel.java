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

public class TownChatChannel extends SimpleChatChannel {

    TownChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, boolean firstMatchingGroup, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
    }

    @Override
    public String processPlaceholders(ChatUser user, String input) {
        return input.replace("<town>", getTown(user));
    }

    private String getTown(ChatUser user) {
        try {
            Resident resident = TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName());

            if (resident.hasTown()) {
                return resident.getTown().getFormattedName();
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        return "";
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

                if (resident.hasTown()) {
                    return resident.getTown().hasResident(sender.asPlayer().getName());
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
    public boolean isTownChat() {
        return true;
    }

    public static TownChatChannel.Builder townBuilder(String name) {
        return new TownChatChannel.Builder(name);
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
        public TownChatChannel build(SimpleChat simpleChat) {
            return new TownChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
        }

        @Override
        public TownChatChannel.Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        @Override
        public TownChatChannel.Builder setColor(String color) {
            return setColor(TextColor.fromHexString(color));
        }

        @Override
        public TownChatChannel.Builder setId(long id) {
            this.id = id;

            return this;
        }

        @Override
        public TownChatChannel.Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

            return this;
        }

        @Override
        public TownChatChannel.Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        @Override
        public TownChatChannel.Builder setIsDefault(boolean aDefault) {
            this.isDefault = aDefault;

            return this;
        }

        @Override
        public TownChatChannel.Builder setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;

            return this;
        }

        @Override
        public TownChatChannel.Builder setName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public TownChatChannel.Builder setDistance(double distance) {
            this.distance = distance;

            return this;
        }

        @Override
        public TownChatChannel.Builder setSwitchMessage(String switchMessage) {
            this.switchMessage = switchMessage;

            return this;
        }

        @Override
        public TownChatChannel.Builder setToggleOffMessage(String toggleOffMessage) {
            this.toggleOffMessage = toggleOffMessage;

            return this;
        }

        @Override
        public TownChatChannel.Builder setToggleOnMessage(String toggleOnMessage) {
            this.toggleOnMessage = toggleOnMessage;

            return this;
        }

        @Override
        public TownChatChannel.Builder setShouldForwardFormatting(boolean forwardFormatting) {
            this.forwardFormatting = forwardFormatting;

            return this;
        }

        @Override
        public TownChatChannel.Builder setShouldBungee(boolean shouldBungee) {
            this.shouldBungee = shouldBungee;

            return this;
        }

        @Override
        public TownChatChannel.Builder setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;

            return this;
        }

        @Override
        public TownChatChannel.Builder setFirstMatchingGroup(boolean firstMatchingGroup) {
            this.firstMatchingGroup = firstMatchingGroup;

            return this;
        }
    }


}
