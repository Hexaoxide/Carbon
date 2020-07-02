package net.draycia.simplechat.channels.impls;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.events.ChatComponentEvent;
import net.draycia.simplechat.events.ChatFormatEvent;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.util.DiscordWebhook;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    private boolean forwardFormatting;
    private boolean shouldBungee;
    private boolean filterEnabled;

    private DiscordWebhook discordWebhook = null;

    private SimpleChat simpleChat;

    private ArrayList<Pattern> itemPatterns = new ArrayList<>();

    private SimpleChatChannel() { }

    SimpleChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, SimpleChat simpleChat) {
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
        this.forwardFormatting = forwardFormatting;
        this.shouldBungee = shouldBungee;
        this.filterEnabled = filterEnabled;

        this.simpleChat = simpleChat;

        if (getChannelId() > 0 && simpleChat.getDiscordManager().getDiscordAPI() != null) {
            simpleChat.getDiscordManager().getDiscordAPI().addMessageCreateListener(this::processDiscordMessage);
        }

        if (getWebhook() != null && !getWebhook().isEmpty()) {
            discordWebhook = new DiscordWebhook(getWebhook());
        }

        for (String entry : simpleChat.getConfig().getStringList("item-link-placeholders")) {
            itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
        }
    }

    SimpleChat getSimpleChat() {
        return simpleChat;
    }

    private String getDiscordFormatting() {
        return formats.getOrDefault("discord-to-mc", "<gray>[<blue>Discord<gray>] <username><white>: <message>");
    }

    @Override
    public boolean canPlayerUse(ChatUser user) {
        return user.asPlayer().hasPermission("simplechat.channels." + getName() + ".use");
    }

    @Override
    public List<ChatUser> getAudience(ChatUser user) {
        List<ChatUser> audience = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canPlayerSee(user, simpleChat.getUserService().wrap(player))) {
                audience.add(simpleChat.getUserService().wrap(player));
            }
        }

        return audience;
    }

    @Override
    public void processDiscordMessage(MessageCreateEvent event) {
        if (event.getChannel().getId() != getChannelId() || !event.getMessageAuthor().isRegularUser()) {
            return;
        }

        String message = MiniMessage.instance().escapeTokens(event.getMessageContent()).replace("~", "\\~")
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
        Component component = MiniMessage.instance().parse(getDiscordFormatting(), "message", message,
                "username", author.getName(), "displayname", author.getDisplayName(), "channel", channel.getName(),
                "server", event.getServer().get().getName(), "primaryrole", role);

        for (ChatUser user : getAudience(null)) {
            user.sendMessage(component);
        }
    }

    @Override
    public void sendMessage(ChatUser user, String message, boolean fromBungee) {
        // Get player's formatting
        String group;

        if (user.isOnline()) {
            group = simpleChat.getPermission().getPrimaryGroup(user.asPlayer());
        } else {
            group = simpleChat.getPermission().getPrimaryGroup(null, user.asOfflinePlayer());
        }

        String messageFormat = getFormat(group);

        // If the player isn't online (cross server message), use their normal name
        if (!user.isOnline()) {
            messageFormat = messageFormat.replace("%player_displayname%", "%player_name%");
        }

        // Chat filter
        if (filterEnabled() && simpleChat.getConfig().contains("filters")) {
            for (String entry : simpleChat.getConfig().getStringList("filters")) {
                message = message.replaceAll(entry, simpleChat.getConfig().getString("filter-text", "****"));
            }
        }

        // Chat placeholders
        ConfigurationSection replacements = simpleChat.getConfig().getConfigurationSection("replacements");

        if (replacements != null) {
            for (String key : replacements.getKeys(false)) {
                message = message.replace(key, replacements.getString(key));
            }
        }

        // Call custom chat event
        ChatFormatEvent formatEvent = new ChatFormatEvent(user, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(formatEvent);

        if (formatEvent.isCancelled()) {
            return;
        }

        // Parse placeholders
        messageFormat = PlaceholderAPI.setPlaceholders(user.asOfflinePlayer(), formatEvent.getFormat());

        // Convert legacy color codes to Mini color codes
        Component component = LegacyComponentSerializer.legacy('&').deserialize(messageFormat);
        messageFormat = MiniMessage.instance().serialize(component);

        // Get formatted message
        TextComponent formattedMessage = (TextComponent)MiniMessage.instance().parse(messageFormat, "color", "<" + color.toString() + ">",
                "phase", Long.toString(System.currentTimeMillis() % 25), "server",
                simpleChat.getConfig().getString("server-name", "Server"),
                "message", formatEvent.getMessage());

        // Handle item linking placeholders
        if (user.isOnline()) {
            // TODO: move this into a ChatComponentEvent listener
            for (Pattern pattern : itemPatterns) {
                formattedMessage = (formattedMessage).replace(pattern, (input) -> {
                    return TextComponent.builder().append(simpleChat.getItemStackUtils().createComponent(user.asPlayer()));
                });
            }
        }

        // Call custom chat event
        ChatComponentEvent componentEvent = new ChatComponentEvent(user, this, formattedMessage, getAudience(user));

        Bukkit.getPluginManager().callEvent(componentEvent);

        if (componentEvent.isCancelled()) {
            return;
        }

        // Handle shadow mutes
        if (user.isShadowMuted()) {
            // TODO: move this into a ChatComponentEvent listener
            if (user.isOnline()) {
                user.sendMessage(componentEvent.getComponent());
            }
        } else {
            // Send message as normal
            for (ChatUser chatUser : componentEvent.getRecipients()) {
                if (message.contains(chatUser.asPlayer().getName())) {
                    if (simpleChat.getConfig().getBoolean("pings.enabled")) {
                        Key key = Key.of(simpleChat.getConfig().getString("pings.sound"));
                        Sound.Source source = Sound.Source.valueOf(simpleChat.getConfig().getString("pings.source"));
                        float volume = (float)simpleChat.getConfig().getDouble("pings.volume");
                        float pitch = (float)simpleChat.getConfig().getDouble("pings.pitch");

                        chatUser.playSound(Sound.of(key, source, volume, pitch));
                    }
                }

                chatUser.sendMessage(componentEvent.getComponent());
            }
        }

        // Log message to console
        String sm = user.isShadowMuted() ? "[SM] " : "";
        System.out.println(sm + LegacyComponentSerializer.legacy().serialize(formattedMessage));

        // Route message to bungee / discord (if message originates from this server)
        // Use instanceof and not isOnline, if this message originates from another then the instanceof will
        // fail, but isOnline may succeed if the player is online on both servers (somehow).
        if (user.isOnline() && fromBungee) {
            if (shouldForwardFormatting() && shouldBungee()) {
                sendMessageToBungee(user.asPlayer(), component);
            } else if (shouldBungee()) {
                sendMessageToBungee(user.asPlayer(), message);
            }

            sendMessageToDiscord(user, message);
        }
    }

    @Override
    public void sendComponent(ChatUser player, Component component) {
        for (ChatUser user : getAudience(player)) {
            user.sendMessage(component);
        }

        System.out.println(LegacyComponentSerializer.legacy().serialize(component));
    }

    public void sendMessageToDiscord(ChatUser user, String message) {
        if (getWebhook() == null || discordWebhook == null) {
            return;
        }

        discordWebhook.setUsername(user.asOfflinePlayer().getName());
        discordWebhook.setContent(message.replace("@", "@\u200B"));
        discordWebhook.setAvatarUrl("https://minotar.net/helm/" + user.getUUID().toString() + "/100.png");

        try {
            discordWebhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToBungee(Player player, Component component) {
        simpleChat.getPluginMessageManager().sendComponent(this, player, component);
    }

    public void sendMessageToBungee(Player player, String message) {
        simpleChat.getPluginMessageManager().sendMessage(this, player, message);
    }

    public boolean canPlayerSee(ChatUser sender, ChatUser target) {
        Player targetPlayer = target.asPlayer();

        if (!targetPlayer.hasPermission("simplechat." + getName().toLowerCase() + ".see")) {
            return false;
        }

        if (sender instanceof Player) {
            if (getDistance() > 0 && targetPlayer.getLocation().distance(((Player)sender).getLocation()) > getDistance()) {
                return false;
            }
        }

        if (isIgnorable() && sender != null) {
            if (target.isIgnoringUser(sender.getUUID()) && !targetPlayer.hasPermission("simplechat.ignoreexempt")) {
                return false;
            }

            if (target.ignoringChannel(this)) {
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
    public String getFormat(String group) {
        return getFormats().getOrDefault(group, getFormats().getOrDefault("default", "<white><%player_displayname%<white>> <message>"));
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
    public boolean shouldBungee() {
        return shouldBungee;
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
    public boolean shouldForwardFormatting() {
        return forwardFormatting;
    }

    @Override
    public boolean filterEnabled() {
        return filterEnabled;
    }

    public static SimpleChatChannel.Builder builder(String name) {
        return new SimpleChatChannel.Builder(name);
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

        private Builder() { }

        Builder(String name) {
            this.name = name.toLowerCase();
        }

        @Override
        public SimpleChatChannel build(SimpleChat simpleChat) {
            return new SimpleChatChannel(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, simpleChat);
        }

        @Override
        public Builder setColor(TextColor color) {
            this.color = color;

            return this;
        }

        @Override
        public Builder setColor(String color) {
            return setColor(TextColor.fromHexString(color));
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

        @Override
        public Builder setShouldForwardFormatting(boolean forwardFormatting) {
            this.forwardFormatting = forwardFormatting;

            return this;
        }

        @Override
        public Builder setShouldBungee(boolean shouldBungee) {
            this.shouldBungee = shouldBungee;

            return this;
        }

        @Override
        public Builder setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;

            return this;
        }
    }

}
