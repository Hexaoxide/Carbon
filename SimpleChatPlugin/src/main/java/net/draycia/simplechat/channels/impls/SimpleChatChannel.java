package net.draycia.simplechat.channels.impls;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.events.ChatComponentEvent;
import net.draycia.simplechat.events.ChatFormatEvent;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;

public class SimpleChatChannel extends ChatChannel {

    private String key;

    private SimpleChat simpleChat;
    private ConfigurationSection config;

    private SimpleChatChannel() { }

    public SimpleChatChannel(String key, SimpleChat simpleChat, ConfigurationSection config) {
        this.key = key;
        this.simpleChat = simpleChat;
        this.config = config;
    }

    public SimpleChat getSimpleChat() {
        return simpleChat;
    }

    @Override
    public Boolean canPlayerUse(ChatUser user) {
        return user.asPlayer().hasPermission("simplechat.channels." + getName() + ".use");
    }

    @Override
    public List<ChatUser> getAudience(ChatUser user) {
        List<ChatUser> audience = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canPlayerSee(user, simpleChat.getUserService().wrap(player), false)) {
                audience.add(simpleChat.getUserService().wrap(player));
            }
        }

        return audience;
    }

    @Override
    public void sendMessage(ChatUser user, String message, boolean fromBungee) {
        // Get player's formatting
        String messageFormat = getFormat(user);

        // Call custom chat event
        ChatFormatEvent formatEvent = new ChatFormatEvent(user, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(formatEvent);

        if (formatEvent.isCancelled()) {
            return;
        }

        // Get formatted message
        TextComponent formattedMessage = (TextComponent)simpleChat.getAdventureManager().processMessage(formatEvent.getFormat(),
                "br", "\n",
                "color", "<color:" + getColor().toString() + ">",
                "phase", Long.toString(System.currentTimeMillis() % 25),
                "server", simpleChat.getConfig().getString("server-name", "Server"),
                "message", formatEvent.getMessage());

        // Call custom chat event
        ChatComponentEvent componentEvent = new ChatComponentEvent(user, this, formattedMessage,
                formatEvent.getMessage(), getAudience(user));

        Bukkit.getPluginManager().callEvent(componentEvent);

        // Send message as normal
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ChatUser chatUser = simpleChat.getUserService().wrap(onlinePlayer);

            TextColor userColor = chatUser.getChannelSettings(this).getColor();

            String prefix = processPlaceholders(user, simpleChat.getConfig().getString("spy-prefix"));

            if (userColor == null) {
                if (isUserSpying(user, chatUser)) {
                    prefix = prefix.replace("<color>", "<color:" + getColor() + ">");

                    chatUser.sendMessage(simpleChat.getAdventureManager().processMessage(prefix).append(componentEvent.getComponent()));
                } else if (componentEvent.getRecipients().contains(chatUser)) {
                    chatUser.sendMessage(componentEvent.getComponent());
                }
            } else {
                prefix = prefix.replace("<color>", "<color:" +
                        chatUser.getChannelSettings(this).getColor().asHexString() + ">");
                String format = formatEvent.getFormat();

                TextComponent newFormat = (TextComponent)simpleChat.getAdventureManager().processMessage(format,
                        "br", "\n",
                        "color", "<color:" + userColor.toString() + ">",
                        "phase", Long.toString(System.currentTimeMillis() % 25),
                        "server", simpleChat.getConfig().getString("server-name", "Server"),
                        "message", formatEvent.getMessage());

                if (isUserSpying(user, chatUser)) {
                    newFormat = (TextComponent)simpleChat.getAdventureManager().processMessage(prefix, "br", "\n")
                            .append(newFormat);
                } else if (!componentEvent.getRecipients().contains(chatUser)) {
                    return;
                }

                ChatComponentEvent newEvent = new ChatComponentEvent(user, this, newFormat,
                        formatEvent.getMessage(), Collections.singletonList(chatUser), true);

                Bukkit.getPluginManager().callEvent(newEvent);

                chatUser.sendMessage(newEvent.getComponent());
            }
        }

        // Log message to console
        String sm = user.isShadowMuted() ? "[SM] " : "";
        System.out.println(sm + LegacyComponentSerializer.legacy().serialize(componentEvent.getComponent()));

        // Route message to bungee / discord (if message originates from this server)
        // Use instanceof and not isOnline, if this message originates from another then the instanceof will
        // fail, but isOnline may succeed if the player is online on both servers (somehow).
        if (user.isOnline() && fromBungee) {
            if (shouldForwardFormatting() && shouldBungee()) {
                sendMessageToBungee(user.asPlayer(), componentEvent.getComponent());
            } else if (shouldBungee()) {
                sendMessageToBungee(user.asPlayer(), message);
            }
        }
    }

    private boolean isUserSpying(ChatUser sender, ChatUser target) {
        if (!canPlayerSee(sender, target, false)) {
            if (target.getChannelSettings(this).isSpying()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void sendComponent(ChatUser player, Component component) {
        for (ChatUser user : getAudience(player)) {
            user.sendMessage(component);
        }

        System.out.println(LegacyComponentSerializer.legacy().serialize(component));
    }

    public void sendMessageToBungee(Player player, Component component) {
        simpleChat.getPluginMessageManager().sendComponent(this, player, component);
    }

    public void sendMessageToBungee(Player player, String message) {
        simpleChat.getPluginMessageManager().sendMessage(this, player, message);
    }

    private String getFormat(ChatUser user) {
        String group = "default";

        if (!firstMatchingGroup()) {
            if (user.isOnline()) {
                group = simpleChat.getPermission().getPrimaryGroup(user.asPlayer());
            } else {
                group = simpleChat.getPermission().getPrimaryGroup(null, user.asOfflinePlayer());
            }
        } else {
            String[] groups;

            if (user.isOnline()) {
                groups = simpleChat.getPermission().getPlayerGroups(user.asPlayer());
            } else {
                groups = simpleChat.getPermission().getPlayerGroups(null, user.asOfflinePlayer());
            }

            for (String entry : groups) {
                if (getFormats().containsKey(entry.toLowerCase())) {
                    group = entry;
                    break;
                }
            }
        }

        return getFormat(group);
    }

    public Boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
        Player targetPlayer = target.asPlayer();

        if (checkSpying && targetPlayer.hasPermission("simplechat.spy." + getName())) {
            if (target.getChannelSettings(this).isSpying()) {
                return true;
            }
        }

        if (!targetPlayer.hasPermission("simplechat.channels." + getName() + ".see")) {
            return false;
        }

        if (sender.isOnline()) {
            if (getDistance() == 0) {
                if (!targetPlayer.getWorld().equals(sender.asPlayer().getWorld())) {
                    return false;
                }
            } else if (getDistance() > 0) {
                Location one = targetPlayer.getLocation();
                Location two = sender.asPlayer().getLocation();

                if (!one.getWorld().equals(two.getWorld())) {
                    return false;
                }

                if (one.distance(two) > getDistance()) {
                    return false;
                }
            }
        }

        if (isIgnorable()) {
            if (target.isIgnoringUser(sender) && !targetPlayer.hasPermission("simplechat.ignore.exempt")) {
                return false;
            }

            if (target.getChannelSettings(this).isIgnored()) {
                return false;
            }
        }

        return true;
    }

    public Map<String, String> getFormats() {
        HashMap<String, String> formats = new HashMap<>();

        ConfigurationSection formatSection = config.getConfigurationSection("formats");

        if (formatSection == null) {
            formatSection = simpleChat.getConfig().getConfigurationSection("default").getConfigurationSection("formats");
        }

        for (String key : formatSection.getKeys(false)) {
            formats.put(key, formatSection.getString(key));
        }

        return formats;
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromHexString(getString("color"));
    }

    @Override
    public String getFormat(String group) {
        return getFormats().getOrDefault(group, getFormats().getOrDefault("default",
                "<white><%player_displayname%<white>> <message>"));
    }

    @Override
    public Boolean isDefault() {
        return getBoolean("default");
    }

    @Override
    public Boolean isIgnorable() {
        return getBoolean("ignorable");
    }

    @Override
    public Boolean shouldBungee() {
        return getBoolean("should-bungee");
    }

    @Override
    public String getName() {
        String name = getString("name");
        return name == null ? key : name;
    }

    @Override
    public Double getDistance() {
        return getDouble("distance");
    }

    @Override
    public String getSwitchMessage() {
        return getString("switch-message");
    }

    @Override
    public String getSwitchOtherMessage() {
        return getString("switch-other-message");
    }

    @Override
    public String getToggleOffMessage() {
        return getString("toggle-off-message");
    }

    @Override
    public String getToggleOnMessage() {
        return getString("toggle-on-message");
    }

    @Override
    public String getToggleOtherOnMessage() {
        return getString("toggle-other-on");
    }

    @Override
    public String getToggleOtherOffMessage() {
        return getString("toggle-other-off");
    }

    @Override
    public String getCannotUseMessage() {
        return getString("cannot-use-channel");
    }

    @Override
    public Boolean shouldForwardFormatting() {
        return getBoolean("forward-format");
    }

    @Override
    public Boolean firstMatchingGroup() {
        return getBoolean("first-matching-group");
    }

    @Override
    public List<Pattern> getItemLinkPatterns() {
        ArrayList<Pattern> itemPatterns = new ArrayList<>();

        for (String entry : simpleChat.getConfig().getStringList("item-link-placeholders")) {
            itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
        }

        return itemPatterns;
    }

    private String getString(String key) {
        if (config != null && config.contains(key)) {
            return config.getString(key);
        }

        ConfigurationSection defaultSection = simpleChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getString(key);
        }

        return null;
    }

    private boolean getBoolean(String key) {
        if (config != null && config.contains(key)) {
            return config.getBoolean(key);
        }

        ConfigurationSection defaultSection = simpleChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getBoolean(key);
        }

        return false;
    }

    private double getDouble(String key) {
        if (config != null && config.contains(key)) {
            return config.getDouble(key);
        }

        ConfigurationSection defaultSection = simpleChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getDouble(key);
        }

        return 0;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getAliases() {
        String aliases = getString("aliases");

        if (aliases == null) {
            return getKey();
        }

        return aliases;
    }

}
