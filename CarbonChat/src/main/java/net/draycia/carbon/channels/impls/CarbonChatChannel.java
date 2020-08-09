package net.draycia.carbon.channels.impls;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class CarbonChatChannel extends ChatChannel {

    private final String key;

    private final CarbonChat carbonChat;
    private final ConfigurationSection config;

    public CarbonChatChannel(String key, CarbonChat carbonChat, ConfigurationSection config) {
        this.key = key;
        this.carbonChat = carbonChat;
        this.config = config;
    }

    public CarbonChat getCarbonChat() {
        return carbonChat;
    }

    @Override
    public boolean testContext(ChatUser sender, ChatUser target) {
        return carbonChat.getContextManager().testContext(sender, target, this);
    }

    @Override
    public Boolean canPlayerUse(ChatUser user) {
        return user.asPlayer().hasPermission("carbonchat.channels." + getName() + ".use");
    }

    @Override
    public List<ChatUser> getAudience(ChatUser user) {
        List<ChatUser> audience = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canPlayerSee(user, carbonChat.getUserService().wrap(player), false)) {
                audience.add(carbonChat.getUserService().wrap(player));
            }
        }

        return audience;
    }

    @Override
    public void sendMessage(ChatUser user, String message, boolean fromBungee) {
        if (user.isOnline()) {
            if (user.getNickname() != null) {
                user.asPlayer().setDisplayName(user.getNickname());
            }
        }

        // Get player's formatting
        String messageFormat = getFormat(user);

        // Call custom chat event
        ChatFormatEvent formatEvent = new ChatFormatEvent(user, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(formatEvent);

        if (formatEvent.isCancelled() || formatEvent.getMessage().isEmpty()) {
            return;
        }

        // Get formatted message
        TextComponent formattedMessage = (TextComponent) carbonChat.getAdventureManager().processMessage(formatEvent.getFormat(),
                "br", "\n",
                "color", "<color:" + getColor().toString() + ">",
                "phase", Long.toString(System.currentTimeMillis() % 25),
                "server", carbonChat.getConfig().getString("server-name", "Server"),
                "message", formatEvent.getMessage());

        // Call custom chat event
        ChatComponentEvent componentEvent = new ChatComponentEvent(user, this, formattedMessage,
                formatEvent.getMessage(), getAudience(user));

        Bukkit.getPluginManager().callEvent(componentEvent);

        // Send message as normal
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ChatUser chatUser = carbonChat.getUserService().wrap(onlinePlayer);

            TextColor userColor = chatUser.getChannelSettings(this).getColor();

            String prefix = processPlaceholders(user, carbonChat.getConfig().getString("spy-prefix"));

            // TODO: restructure plugin to always format messages per player

            if (userColor == null) {
                if (isUserSpying(user, chatUser)) {
                    prefix = prefix.replace("<color>", "<color:" + getColor() + ">");

                    chatUser.sendMessage(carbonChat.getAdventureManager().processMessage(prefix).append(componentEvent.getComponent()));
                } else if (componentEvent.getRecipients().contains(chatUser)) {
                    chatUser.sendMessage(componentEvent.getComponent());
                }
            } else {
                prefix = prefix.replace("<color>", "<color:" +
                        chatUser.getChannelSettings(this).getColor().asHexString() + ">");
                String format = formatEvent.getFormat();

                TextComponent newFormat = (TextComponent) carbonChat.getAdventureManager().processMessage(format,
                        "br", "\n",
                        "color", "<color:" + userColor.toString() + ">",
                        "phase", Long.toString(System.currentTimeMillis() % 25),
                        "server", carbonChat.getConfig().getString("server-name", "Server"),
                        "message", formatEvent.getMessage());

                if (isUserSpying(user, chatUser)) {
                    newFormat = (TextComponent) carbonChat.getAdventureManager().processMessage(prefix, "br", "\n")
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
        System.out.println(sm + CarbonChat.LEGACY.serialize(componentEvent.getComponent()));

        // Route message to bungee / discord (if message originates from this server)
        // Use instanceof and not isOnline, if this message originates from another then the instanceof will
        // fail, but isOnline may succeed if the player is online on both servers (somehow).
        if (user.isOnline() && !fromBungee && shouldBungee()) {
            if (shouldForwardFormatting()) {
                sendMessageToBungee(user.asPlayer(), componentEvent.getComponent());
            } else {
                sendMessageToBungee(user.asPlayer(), message);
            }
        }
    }

    public String getFormat(ChatUser user) {
        if (primaryGroupOnly()) {
            return getPrimaryGroupFormat(user);
        } else {
            return getFirstFoundUserFormat(user);
        }
    }

    private String getFirstFoundUserFormat(ChatUser user) {
        String[] playerGroups;

        if (user.isOnline()) {
            playerGroups = carbonChat.getPermission().getPlayerGroups(user.asPlayer());
        } else {
            playerGroups = carbonChat.getPermission().getPlayerGroups(null, user.asOfflinePlayer());
        }

        for (String group : playerGroups) {
            String groupFormat = getFormat(group);

            if (groupFormat != null) {
                return groupFormat;
            }
        }

        return getDefaultFormat();
    }

    private String getPrimaryGroupFormat(ChatUser user) {
        String primaryGroup;

        if (user.isOnline()) {
            primaryGroup = carbonChat.getPermission().getPrimaryGroup(user.asPlayer());
        } else {
            primaryGroup = carbonChat.getPermission().getPrimaryGroup(null, user.asOfflinePlayer());
        }

        String primaryGroupFormat = getFormat(primaryGroup);

        if (primaryGroupFormat != null) {
            return primaryGroupFormat;
        }

        return getDefaultFormat();
    }

    private String getDefaultFormat() {
        return getFormat(getDefaultFormatName());
    }

    @Override
    public String getFormat(String group) {
        return getString("formats." + group);
    }

    private boolean isUserSpying(ChatUser sender, ChatUser target) {
        if (!canPlayerSee(sender, target, false)) {
            return target.getChannelSettings(this).isSpying();
        }

        return false;
    }

    @Override
    public void sendComponent(ChatUser player, Component component) {
        for (ChatUser user : getAudience(player)) {
            if (!user.isIgnoringUser(player)) {
                user.sendMessage(component);
            }
        }

        System.out.println(CarbonChat.LEGACY.serialize(component));
    }

    public void sendMessageToBungee(Player player, Component component) {
        carbonChat.getPluginMessageManager().sendComponent(this, player, component);
    }

    public void sendMessageToBungee(Player player, String message) {
        carbonChat.getPluginMessageManager().sendMessage(this, player, message);
    }

    public Boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
        Player targetPlayer = target.asPlayer();

        if (checkSpying && targetPlayer.hasPermission("carbonchat.spy." + getName())) {
            if (target.getChannelSettings(this).isSpying()) {
                return true;
            }
        }

        if (!targetPlayer.hasPermission("carbonchat.channels." + getName() + ".see")) {
            return false;
        }

        if (isIgnorable()) {
            if (target.isIgnoringUser(sender) && !targetPlayer.hasPermission("carbonchat.ignore.exempt")) {
                return false;
            }

            if (target.getChannelSettings(this).isIgnored()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromHexString(getString("color"));
    }

    private String getDefaultFormatName() {
        return getString("default-group");
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
    @Nullable
    public String getMessagePrefix() {
        if (config != null && config.contains("message-prefix")) {
            return config.getString("message-prefix");
        }

        return null;
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
    public Boolean primaryGroupOnly() {
        return getBoolean("primary-group-only");
    }

    @Override
    public List<Pattern> getItemLinkPatterns() {
        ArrayList<Pattern> itemPatterns = new ArrayList<>();

        for (String entry : carbonChat.getConfig().getStringList("item-link-placeholders")) {
            itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
        }

        return itemPatterns;
    }

    @Override
    public Object getContext(String key) {
        ConfigurationSection section = config.getConfigurationSection("contexts");

        if (section == null) {
            ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

            if (defaultSection == null) {
                return null;
            }

            ConfigurationSection defaultContexts = defaultSection.getConfigurationSection("contexts");

            if (defaultContexts == null) {
                return null;
            }

            return defaultContexts.get(key);
        }

        return section.get(key);
    }

    private String getString(String key) {
        if (config != null && config.contains(key)) {
            return config.getString(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getString(key);
        }

        return null;
    }

    private boolean getBoolean(String key) {
        if (config != null && config.contains(key)) {
            return config.getBoolean(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getBoolean(key);
        }

        return false;
    }

    private double getDouble(String key) {
        if (config != null && config.contains(key)) {
            return config.getDouble(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

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
