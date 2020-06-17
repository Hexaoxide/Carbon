package net.draycia.simplechat.channels.impls;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.gmail.nossr50.api.PartyAPI;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import me.clip.placeholderapi.PlaceholderAPI;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
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

    private WebhookClient webhookClient = null;

    private SimpleChat simpleChat;

    private SimpleChatChannel() { }

    SimpleChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, SimpleChat simpleChat) {
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

        this.simpleChat = simpleChat;

        if (getChannelId() > 0 && simpleChat.getDiscordAPI() != null) {
            simpleChat.getDiscordAPI().addMessageCreateListener(this::processDiscordMessage);
        }

        if (getWebhook() != null) {
            webhookClient = WebhookClient.withUrl(getWebhook());
        }
    }

    private String getDiscordFormatting() {
        return formats.getOrDefault("discord-to-mc", "<gray>[<blue>Discord<gray>] <username><white>: <message>");
    }

    @Override
    public boolean canPlayerUse(Player player) {
        return player.hasPermission("simplechat.channels." + getName());
        // TODO: other use conditions
    }

    @Override
    public void processDiscordMessage(MessageCreateEvent event) {
        if (event.getChannel().getId() != getChannelId() || !event.getMessageAuthor().isRegularUser()) {
            return;
        }

        String message = MiniMessageParser.escapeTokens(event.getMessageContent()).replace("~", "\\~")
                .replace("_", "\\_").replace("*", "\\*").replace("\n", "");

        MessageAuthor author = event.getMessageAuthor();
        ServerTextChannel channel = (ServerTextChannel)event.getChannel();

        String role = "";

        if (author.asUser().isPresent() && event.getServer().isPresent()) {
            List<Role> roles = author.asUser().get().getRoles(event.getServer().get());

            if (!roles.isEmpty()) {
                role = roles.get(roles.size() - 1).getName();
            }
        }

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

        System.out.println(messageFormat);

        ChannelChatEvent event = new ChannelChatEvent(player, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        messageFormat = PlaceholderAPI.setPlaceholders(player, event.getFormat());
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "color", "<" + color.toString() + ">");
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "message", event.getMessage());

        // TODO: unfuck this
        Component formattedMessage = /*TextUtilsKt.removeEscape(*/MiniMessageParser.parseFormat(messageFormat)/*, '\\')*/;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!canUserSeeMessage(player, onlinePlayer)) {
                continue;
            }

            TextAdapter.sendMessage(onlinePlayer, formattedMessage);
        }

        System.out.println(LegacyComponentSerializer.INSTANCE.serialize(formattedMessage));

        sendMessageToDiscord(player, message);
    }

    public void sendMessageToDiscord(Player player, String message) {
        if (webhookClient == null) {
            return;
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        builder.setUsername(player.getName());
        builder.setContent(message.replace("@", "@\u200B"));
        builder.setAvatarUrl("https://minotar.net/helm/" + player.getUniqueId().toString() + "/100.png");

        webhookClient.send(builder.build());
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

    public Map<String, String> getFormats() {
        return formats;
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

    public static SimpleChatChannel.Builder builder(String name) {
        return new SimpleChatChannel.Builder(name);
    }

    public static class Builder extends ChatChannel.Builder {

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

        private Builder() { }

        private Builder(String name) {
            this.name = name.toLowerCase();
        }

        @Override
        public SimpleChatChannel build(SimpleChat simpleChat) {
            return new SimpleChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, simpleChat);
        }

        @Override
        public Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        @Override
        public Builder setId(long id) {
            this.id = id;

            return this;
        }

        @Override
        public Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

            return this;
        }

        @Override
        public Builder setWebhook(String webhook) {
            this.webhook = webhook;

            return this;
        }

        @Override
        public Builder setIsDefault(boolean aDefault) {
            this.isDefault = aDefault;

            return this;
        }

        @Override
        public Builder setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;

            return this;
        }

        @Override
        public Builder setName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public Builder setDistance(double distance) {
            this.distance = distance;

            return this;
        }

        @Override
        public Builder setSwitchMessage(String switchMessage) {
            this.switchMessage = switchMessage;

            return this;
        }

        @Override
        public Builder setToggleOffMessage(String toggleOffMessage) {
            this.toggleOffMessage = toggleOffMessage;

            return this;
        }

        @Override
        public Builder setToggleOnMessage(String toggleOnMessage) {
            this.toggleOnMessage = toggleOnMessage;

            return this;
        }
    }

}
