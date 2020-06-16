package net.draycia.simplechat.channels;

import com.gmail.nossr50.api.PartyAPI;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.clip.placeholderapi.PlaceholderAPI;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChannelChatEvent;
import net.kyori.text.Component;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.format.TextColor;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleChatChannel extends ChatChannel {

    private TextColor color;
    private long id;
    private Map<String, String> formats;
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

    private SimpleChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean isTownChat, boolean isNationChat, boolean isAllianceChat, boolean isPartyChat, SimpleChat simpleChat) {
        this.color = color;
        this.id = id;
        this.formats = formats;
        this.webhook = webhook;
        this.isDefault = isDefault;
        this.ignorable = ignorable;
        this.name = name;
        this.distance = distance;
        this.switchMessage = switchMessage;
        this.toggleOffMessage = toggleOffMessage;
        this.toggleOnMessage = toggleOnMessage;

        this.isTownChat = isTownChat;
        this.isNationChat = isNationChat;
        this.isAllianceChat = isAllianceChat;
        this.isPartyChat = isPartyChat;

        this.simpleChat = simpleChat;

        if (getChannelId() > 0 && simpleChat.getDiscordAPI() != null) {
            simpleChat.getDiscordAPI().addMessageCreateListener(this::processDiscordMessage);
        }

        // TODO: webhooks
    }

    private String getDiscordFormatting() {
        return formats.getOrDefault("discord-to-mc", "<gray>[<blue>Discord<gray>] <username>: <white><message>");
    }

    @Override
    public boolean canPlayerUse(Player player) {
        return player.hasPermission("simplechat.channels." + getName());
        // TODO: other use conditions
    }

    @Override
    public void processDiscordMessage(MessageCreateEvent event) {
        if (event.getChannel().getId() != getChannelId() || event.getMessageAuthor().isBotUser()) {
            return;
        }

        String message = MiniMessageParser.escapeTokens(event.getMessageContent()).replace("~", "\\~")
                .replace("_", "\\_").replace("*", "\\*").replace("\n", "");

        MessageAuthor author = event.getMessageAuthor();
        ServerTextChannel channel = (ServerTextChannel)event.getChannel();

        List<Role> roles = author.asUser().get().getRoles(event.getServer().get());

        String role = roles.get(roles.size() - 1).getName();

        // Placeholders: username, displayname, channel, server, message, primaryrole
        Component component = MiniMessageParser.parseFormat(getDiscordFormatting(), "message", message,
                "username", author.getName(), "displayname", author.getDisplayName(), "channel", channel.getName(),
                "server", event.getServer().get().getName(), "primaryrole", role);

        for (Player player : Bukkit.getOnlinePlayers()) {
            TextAdapter.sendMessage(player, component);
        }
    }

    @Override
    public void sendMessage(Player player, String message) {
        message = MiniMessageParser.escapeTokens(message);

        String messageFormat = getFormat(player);

        ChannelChatEvent event = new ChannelChatEvent(player, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        messageFormat = PlaceholderAPI.setPlaceholders(player, event.getFormat());
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "color", "<" + color.toString() + ">");
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "message", event.getMessage());

        Component formattedMessage = /*TextUtilsKt.removeEscape(*/MiniMessageParser.parseFormat(messageFormat)/*, '\\')*/;

        if (isTownChat()) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());

                if (resident.hasTown()) {
                    Town town = resident.getTown();

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (!town.hasResident(onlinePlayer.getName())) {
                            continue;
                        }

                        if (!canUserSeeMessage(player, onlinePlayer)) {
                            continue;
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
                            continue;
                        }

                        if (!canUserSeeMessage(player, onlinePlayer)) {
                            continue;
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
                        continue;
                    }

                    if (!canUserSeeMessage(player, onlinePlayer)) {
                        continue;
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
                        continue;
                    }

                    if (!canUserSeeMessage(player, onlinePlayer)) {
                        continue;
                    }

                    TextAdapter.sendMessage(onlinePlayer, formattedMessage);
                }
            } else {
                // TODO: send "you don't belong to a party" message
                return;
            }
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!canUserSeeMessage(player, onlinePlayer)) {
                    continue;
                }

                TextAdapter.sendMessage(onlinePlayer, formattedMessage);
            }
        }

        System.out.println(LegacyComponentSerializer.INSTANCE.serialize(formattedMessage));
    }

    public boolean canUserSeeMessage(Player sender, Player target) {
        if (!target.hasPermission("simplechat.see." + getName().toLowerCase())) {
            return false;
        }

        if (getDistance() > 0 && target.getLocation().distance(sender.getLocation()) > getDistance()) {
            return false;
        }

        if (isIgnorable()) {
            if (simpleChat.playerHasPlayerIgnored(sender, target)) {
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
    public String getFormat(Player player) {
        String group = simpleChat.getPermission().getPrimaryGroup(player);

        return formats.getOrDefault(group, formats.getOrDefault("default", "<white><%player_displayname%<white>> <message>"));
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
        private Map<String, String> formats = new HashMap<>();
        private String webhook = null;
        private boolean isDefault = false;
        private boolean ignorable = true;
        private String name;
        private double distance = -1;
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
            return new SimpleChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, isTownChat, isNationChat, isAllianceChat, isPartyChat, simpleChat);
        }

        public Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        public Builder setId(long id) {
            this.id = id;

            return this;
        }

        public Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

            return this;
        }

        public Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        public Builder setIsDefault(boolean aDefault) {
            this.isDefault = aDefault;

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
