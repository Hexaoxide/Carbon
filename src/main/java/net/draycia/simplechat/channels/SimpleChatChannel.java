package net.draycia.simplechat.channels;

import com.earth2me.essentials.Essentials;
import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.mcMMO;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.clip.placeholderapi.PlaceholderAPI;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.TextUtilsKt;
import net.draycia.simplechat.events.ChannelChatEvent;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.format.TextColor;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SimpleChatChannel extends ChatChannel {

    private TextColor color;
    private long id;
    private String format;
    private String staffFormat;
    private String webhook;
    private boolean isDefault;
    private boolean ignorable;
    private String name;
    private double distance;
    private String switchMessage;
    private String toggleOffMessage;
    private String toggleOnMessage;

    // Plugin hooks
    private boolean isTownChat;
    private boolean isNationChat;
    private boolean isAllianceChat;
    private boolean isPartyChat;

    private SimpleChat simpleChat;

    private SimpleChatChannel() { }

    private SimpleChatChannel(TextColor color, long id, String format, String staffFormat, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, boolean isTownChat, boolean isNationChat, boolean isAllianceChat, boolean isPartyChat, SimpleChat simpleChat) {
        this.color = color;
        this.id = id;
        this.format = format;
        this.staffFormat = staffFormat;
        this.webhook = webhook;
        this.isDefault = isDefault;
        this.ignorable = ignorable;
        this.name = name;
        this.distance = distance;
        this.switchMessage = switchMessage;

        this.isTownChat = isTownChat;
        this.isNationChat = isNationChat;
        this.isAllianceChat = isAllianceChat;
        this.isPartyChat = isPartyChat;

        this.simpleChat = simpleChat;
    }

    @Override
    public boolean canPlayerUse(Player player) {
        if (player.hasPermission("simplechat.channels." + getName())) {
            return true;
        }

        // TODO: other use conditions
        return false;
    }

    @Override
    public void sendMessage(Player player, String message) {
        message = MiniMessageParser.escapeTokens(message);

        String messageFormat;

        if (player.hasPermission("simplechat.staff")) {
            messageFormat = getStaffFormat();
        } else {
            messageFormat = getFormat();
        }

        ChannelChatEvent event = new ChannelChatEvent(player, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        messageFormat = PlaceholderAPI.setPlaceholders(player, event.getFormat());
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "color", "<" + color.toString() + ">");
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "message", event.getMessage());

        Component formattedMessage = TextUtilsKt.removeEscape(MiniMessageParser.parseFormat(messageFormat), '\\');

        if (isTownChat()) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());

                if (resident.hasTown()) {
                    Town town = resident.getTown();

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (!town.hasResident(onlinePlayer.getName())) {
                            return;
                        }

                        if (!canUserSeeMessage(player, onlinePlayer)) {
                            return;
                        }

                        TextAdapter.sendMessage(onlinePlayer, formattedMessage);
                    }
                } else {
                    // TODO: send "you don't belong to a town" message
                    return;
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
                return;
            }
        } else if (isNationChat()) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());

                if (resident.hasNation()) {
                    Nation nation = resident.getTown().getNation();

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (!nation.hasResident(onlinePlayer.getName())) {
                            return;
                        }

                        if (!canUserSeeMessage(player, onlinePlayer)) {
                            return;
                        }

                        TextAdapter.sendMessage(onlinePlayer, formattedMessage);
                    }
                } else {
                    // TODO: send "you don't belong to a nation" message
                    return;
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
                return;
            }
        } else if (isAllianceChat()) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Resident target = TownyAPI.getInstance().getDataSource().getResident(onlinePlayer.getName());

                    if (!resident.isAlliedWith(target)) {
                        return;
                    }

                    if (!canUserSeeMessage(player, onlinePlayer)) {
                        return;
                    }

                    TextAdapter.sendMessage(onlinePlayer, formattedMessage);
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
                return;
            }
        } else if (isPartyChat()) {
            if (PartyAPI.inParty(player)) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!PartyAPI.inSameParty(player, onlinePlayer)) {
                        return;
                    }

                    if (!canUserSeeMessage(player, onlinePlayer)) {
                        return;
                    }

                    TextAdapter.sendMessage(onlinePlayer, formattedMessage);
                }
            } else {
                // TODO: send "you don't belong to a party" message
                return;
            }
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("simplechat.see." + getName().toLowerCase())) {
                    if (!canUserSeeMessage(player, onlinePlayer)) {
                        return;
                    }

                    TextAdapter.sendMessage(onlinePlayer, formattedMessage);
                }
            }
        }


        System.out.println(LegacyComponentSerializer.INSTANCE.serialize(formattedMessage));
    }

    private boolean isUserIgnoringUser(Player user, Player target) {
        Plugin essentials = Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        if (essentials != null) {
            Essentials essx = (Essentials)essentials;

            return essx.getUser(user).isIgnoredPlayer(essx.getUser(target));
        }

        return false;
    }

    public boolean canUserSeeMessage(Player sender, Player target) {
        if (getDistance() > 0 && target.getLocation().distance(sender.getLocation()) > getDistance()) {
            return false;
        }

        if (isIgnorable()) {
            if (isUserIgnoringUser(sender, target)) {
                return false;
            }

            if (simpleChat.playerHasChannelMuted(target, this)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public TextColor getColor() {
        return color;
    }

    @Override
    public long getChannelId() {
        return id;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getStaffFormat() {
        if (staffFormat == null) {
            return format;
        }

        return staffFormat;
    }

    @Override
    public String getWebhook() {
        return webhook;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean isIgnorable() {
        return ignorable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public String getSwitchMessage() {
        return switchMessage;
    }

    @Override
    public String getToggleOffMessage() {
        return toggleOffMessage;
    }

    @Override
    public String getToggleOnMessage() {
        return toggleOnMessage;
    }

    @Override
    public boolean isTownChat() {
        return isTownChat;
    }

    @Override
    public boolean isNationChat() {
        return isNationChat;
    }

    @Override
    public boolean isAllianceChat() {
        return isAllianceChat;
    }

    @Override
    public boolean isPartyChat() {
        return isPartyChat;
    }

    public static SimpleChatChannel.Builder builder(String name) {
        return new SimpleChatChannel.Builder(name);
    }

    public static class Builder {

        private TextColor color = TextColor.WHITE;
        private long id = -1;
        private String format = "<<white><%player_displayname%<white>>";
        private String staffFormat = null;
        private String webhook = null;
        private boolean isDefault = false;
        private boolean ignorable = true;
        private String name;
        private double distance = 0;
        private String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";
        private String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";
        private String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";
        private boolean isTownChat = false;
        private boolean isNationChat = false;
        private boolean isAllianceChat = false;
        private boolean isPartyChat = false;

        private Builder() { }

        private Builder(String name) {
            this.name = name.toLowerCase();
        }

        public SimpleChatChannel build(SimpleChat simpleChat) {
            return new SimpleChatChannel(color, id, format, staffFormat, webhook, isDefault, ignorable, name, distance, switchMessage, isTownChat, isNationChat, isAllianceChat, isPartyChat, simpleChat);
        }

        public Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        public Builder setId(long id) {
            this.id = id;

            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;

            return this;
        }

        public Builder setStaffFormat(String staffFormat) {
            this.staffFormat = staffFormat;

            return this;
        }

        public Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        public Builder setIsDefault(boolean aDefault) {
            isDefault = aDefault;

            return this;
        }

        public Builder setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;

            return this;
        }

        public Builder setName(String name) {
            this.name = name;

            return this;
        }

        public Builder setDistance(double distance) {
            this.distance = distance;

            return this;
        }

        public Builder setSwitchMessage(String switchMessage) {
            this.switchMessage = switchMessage;

            return this;
        }

        public Builder setToggleOffMessage(String toggleOffMessage) {
            this.toggleOffMessage = toggleOffMessage;

            return this;
        }

        public Builder setToggleOnMessage(String toggleOnMessage) {
            this.toggleOnMessage = toggleOnMessage;

            return this;
        }

        public Builder setIsTownChat(boolean isTownChat) {
            this.isTownChat = isTownChat;

            return this;
        }

        public Builder setIsNationChat(boolean isNationChat) {
            this.isNationChat = isNationChat;

            return this;
        }

        public Builder setIsAllianceChat(boolean isAllianceChat) {
            this.isAllianceChat = isAllianceChat;

            return this;
        }

        public Builder setIsPartyChat(boolean isPartyChat) {
            this.isPartyChat = isPartyChat;

            return this;
        }
    }

}
