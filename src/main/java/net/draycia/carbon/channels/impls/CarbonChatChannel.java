package net.draycia.carbon.channels.impls;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CarbonChatChannel extends ChatChannel {

    @NonNull
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("<message>");

    @NonNull
    private final String key;

    @NonNull
    private final CarbonChat carbonChat;

    @Nullable
    private final ConfigurationSection config;

    public CarbonChatChannel(@NonNull String key, @NonNull CarbonChat carbonChat, @Nullable ConfigurationSection config) {
        this.key = key;
        this.carbonChat = carbonChat;
        this.config = config;
    }

    @NonNull
    public CarbonChat getCarbonChat() {
        return carbonChat;
    }

    @Override
    public boolean testContext(@NonNull ChatUser sender, @NonNull ChatUser target) {
        return carbonChat.getContextManager().testContext(sender, target, this);
    }

    @Override
    public boolean canPlayerUse(@NonNull ChatUser user) {
        return user.asPlayer().hasPermission("carbonchat.channels." + getName() + ".use");
    }

    @Override
    @NonNull
    public List<@NonNull ChatUser> audiences() {
        List<ChatUser> audience = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUser playerUser = carbonChat.getUserService().wrap(player);

            if (canPlayerSee(playerUser, true)) {
                audience.add(playerUser);
            }
        }

        return audience;
    }

    private void updateUserNickname(@NonNull ChatUser user) {
        if (user.isOnline()) {
            String nickname = user.getNickname();

            if (nickname != null) {
                Component component = carbonChat.getAdventureManager().processMessage(nickname);
                nickname = CarbonChat.LEGACY.serialize(component);

                user.asPlayer().setDisplayName(nickname);

                if (carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
                    user.asPlayer().setPlayerListName(nickname);
                }
            }
        }
    }

    @Override
    @NonNull
    public Component sendMessage(@NonNull ChatUser user, @NonNull Collection<@NonNull ChatUser> recipients, @NonNull String message, boolean fromRemote) {
        updateUserNickname(user);

        // Get player's formatting
        String messageFormat = getFormat(user);

        // Call custom chat event
        PreChatFormatEvent preFormatEvent = new PreChatFormatEvent(user, this, messageFormat, message);
        Bukkit.getPluginManager().callEvent(preFormatEvent);

        // Return if cancelled or message is emptied

        if (preFormatEvent.isCancelled() || preFormatEvent.getMessage().trim().isEmpty()) {
            return TextComponent.empty();
        }

        String displayName;

        if (user.getNickname() != null) {
            displayName = user.getNickname();
        } else {
            if (user.isOnline()) {
                displayName = user.asPlayer().getDisplayName();
            } else {
                displayName = user.asOfflinePlayer().getName();
            }
        }

        // Iterate through players who should receive messages in this channel
        for (ChatUser target : recipients) {
            // Call second format event. Used for relational stuff (placeholders etc)
            ChatFormatEvent formatEvent = new ChatFormatEvent(user, target, this, preFormatEvent.getFormat(), preFormatEvent.getMessage());
            Bukkit.getPluginManager().callEvent(formatEvent);

            // Again, return if cancelled or message is emptied
            if (formatEvent.isCancelled() || formatEvent.getMessage().trim().isEmpty()) {
                continue;
            }

            TextColor targetColor = getChannelColor(target);

            TextComponent formatComponent = (TextComponent) carbonChat.getAdventureManager().processMessage(formatEvent.getFormat(),
                    "br", "\n",
                    "displayname", displayName,
                    "color", "<" + targetColor.asHexString() + ">",
                    "phase", Long.toString(System.currentTimeMillis() % 25),
                    "server", carbonChat.getConfig().getString("server-name", "Server"),
                    "message", formatEvent.getMessage());

            if (isUserSpying(user, target)) {
                String prefix = processPlaceholders(user, carbonChat.getConfig().getString("spy-prefix"));

                formatComponent = (TextComponent) MiniMessage.get().parse(prefix, "color",
                        targetColor.asHexString()).append(formatComponent);
            }

            ChatComponentEvent newEvent = new ChatComponentEvent(user, target, this, formatComponent,
                    formatEvent.getMessage());

            Bukkit.getPluginManager().callEvent(newEvent);

            target.sendMessage(newEvent.getComponent());
        }

        ChatFormatEvent consoleFormatEvent = new ChatFormatEvent(user, null, this, preFormatEvent.getFormat(),
                preFormatEvent.getMessage());

        Bukkit.getPluginManager().callEvent(consoleFormatEvent);

        TextColor targetColor = getChannelColor(user);

        TextComponent consoleFormat = (TextComponent) carbonChat.getAdventureManager().processMessage(consoleFormatEvent.getFormat(),
                "br", "\n",
                "displayname", displayName,
                "color", "<" + targetColor.asHexString() + ">",
                "phase", Long.toString(System.currentTimeMillis() % 25),
                "server", carbonChat.getConfig().getString("server-name", "Server"),
                "message", consoleFormatEvent.getMessage());

        ChatComponentEvent consoleEvent = new ChatComponentEvent(user, null, this, consoleFormat,
                consoleFormatEvent.getMessage());

        Bukkit.getPluginManager().callEvent(consoleEvent);

        // Route message to bungee / discord (if message originates from this server)
        // Use instanceof and not isOnline, if this message originates from another then the instanceof will
        // fail, but isOnline may succeed if the player is online on both servers (somehow).
        if (user.isOnline() && !fromRemote && (shouldBungee() || isCrossServer())) {
            sendMessageToBungee(user.asPlayer(), consoleEvent.getComponent());
        }

        return consoleEvent.getComponent();
    }

    @Override
    @NonNull
    public Component sendMessage(@NonNull ChatUser user, @NonNull String message, boolean fromRemote) {
        return this.sendMessage(user, this.audiences(), message, fromRemote);
    }

    @Nullable
    public String getFormat(@NonNull ChatUser user) {
        for (String group : this.getGroupOverrides()) {
            if (userHasGroup(user, group)) {
                String format = getFormat(group);

                if (format != null) {
                    return format;
                }
            }
        }

        if (primaryGroupOnly()) {
            return getPrimaryGroupFormat(user);
        } else {
            return getFirstFoundUserFormat(user);
        }
    }

    private boolean userHasGroup(@NonNull ChatUser user, @NonNull String group) {
        if (user.isOnline()) {
            if (carbonChat.getPermission().playerInGroup(user.asPlayer(), group)) {
                return true;
            }
        } else {
            if (carbonChat.getPermission().playerInGroup(null, user.asOfflinePlayer(), group)) {
                return true;
            }
        }

        if (user.isOnline() && this.permissionGroupMatching()) {
            return user.asPlayer().hasPermission("carbonchat.group." + group);
        }

        return false;
    }

    @Nullable
    private String getFirstFoundUserFormat(@NonNull ChatUser user) {
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

    @Nullable
    private String getPrimaryGroupFormat(@NonNull ChatUser user) {
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

    @Nullable
    private String getDefaultFormat() {
        return getFormat(getDefaultFormatName());
    }

    @Override
    @Nullable
    public String getFormat(@NonNull String group) {
        return getString("formats." + group);
    }

    private boolean isUserSpying(@NonNull ChatUser sender, @NonNull ChatUser target) {
        if (!canPlayerSee(sender, target, false)) {
            return target.getChannelSettings(this).isSpying();
        }

        return false;
    }

    @Override
    public void sendComponent(@NonNull ChatUser player, @NonNull Component component) {
        for (ChatUser user : audiences()) {
            if (!user.isIgnoringUser(player)) {
                user.sendMessage(component);
            }
        }
    }

    public void sendMessageToBungee(@NonNull Player player, @NonNull Component component) {
        carbonChat.getMessageManager().sendMessage("channel-component", player.getUniqueId(), (byteArray) -> {
            byteArray.writeUTF(this.getKey());
            byteArray.writeUTF(carbonChat.getAdventureManager().getAudiences().gsonSerializer().serialize(component));
        });
    }

    @Override
    public boolean canPlayerSee(@NonNull ChatUser target, boolean checkSpying) {
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
            return !target.getChannelSettings(this).isIgnored();
        }

        return true;
    }

    public boolean canPlayerSee(@NonNull ChatUser sender, @NonNull ChatUser target, boolean checkSpying) {
        Player targetPlayer = target.asPlayer();

        if (!canPlayerSee(target, checkSpying)) {
            return false;
        }

        if (isIgnorable()) {
            return !target.isIgnoringUser(sender) || targetPlayer.hasPermission("carbonchat.ignore.exempt");
        }

        return true;
    }

    @Override
    @Nullable
    public TextColor getChannelColor(@NonNull ChatUser user) {
        TextColor userColor = user.getChannelSettings(this).getColor();

        if (userColor != null) {
            System.out.println("user color found!");
            return userColor;
        }

        String input = getString("color");

        TextColor color = CarbonUtils.parseColor(user, input);

        if (color == null && carbonChat.getConfig().getBoolean("show-tips")) {
            carbonChat.getLogger().warning("Tip: Channel color found (" + color + ") is invalid!");
            carbonChat.getLogger().warning("Falling back to #FFFFFF");

            return NamedTextColor.WHITE;
        }

        return color;
    }

    @Nullable
    private String getDefaultFormatName() {
        return getString("default-group");
    }

    @Override
    public boolean isDefault() {
        if (config != null && config.contains("default")) {
            return config.getBoolean("default");
        }

        return false;
    }

    @Override
    public boolean isIgnorable() {
        return getBoolean("ignorable");
    }

    @Override
    @Deprecated
    public boolean shouldBungee() {
        return getBoolean("should-bungee");
    }

    @Override
    public boolean isCrossServer() {
        return getBoolean("is-cross-server");
    }

    @Override
    public boolean honorsRecipientList() {
        return getBoolean("honors-recipient-list");
    }

    @Override
    public boolean permissionGroupMatching() {
        return getBoolean("permission-group-matching");
    }

    @Override
    @NonNull
    public List<@NonNull String> getGroupOverrides() {
        return getStringList("group-overrides");
    }

    @Override
    @NonNull
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
    @Nullable
    public String getSwitchMessage() {
        return getString("switch-message");
    }

    @Override
    @Nullable
    public String getSwitchOtherMessage() {
        return getString("switch-other-message");
    }

    @Override
    @Nullable
    public String getSwitchFailureMessage() {
        return getString("switch-failure-message");
    }

    @Override
    @Nullable
    public String getCannotIgnoreMessage() {
        return getString("cannot-ignore-message");
    }

    @Override
    @Nullable
    public String getToggleOffMessage() {
        return getString("toggle-off-message");
    }

    @Override
    @Nullable
    public String getToggleOnMessage() {
        return getString("toggle-on-message");
    }

    @Override
    @Nullable
    public String getToggleOtherOnMessage() {
        return getString("toggle-other-on");
    }

    @Override
    @Nullable
    public String getToggleOtherOffMessage() {
        return getString("toggle-other-off");
    }

    @Override
    @Nullable
    public String getCannotUseMessage() {
        return getString("cannot-use-channel");
    }

    @Override
    public boolean shouldForwardFormatting() {
        return getBoolean("forward-format");
    }

    @Override
    public boolean primaryGroupOnly() {
        return getBoolean("primary-group-only");
    }

    @Override
    @NonNull
    public List<@NonNull Pattern> getItemLinkPatterns() {
        ArrayList<Pattern> itemPatterns = new ArrayList<>();

        for (String entry : carbonChat.getConfig().getStringList("item-link-placeholders")) {
            itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
        }

        return itemPatterns;
    }

    @Override
    @Nullable
    public Object getContext(@NonNull String key) {
        ConfigurationSection section = config == null ? null : config.getConfigurationSection("contexts");

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

    @Nullable
    private String getString(@NonNull String key) {
        if (config != null && config.contains(key)) {
            return config.getString(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getString(key);
        }

        return null;
    }

    @NonNull
    private List<@NonNull String> getStringList(@NonNull String key) {
        if (config != null && config.contains(key)) {
            return config.getStringList(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getStringList(key);
        }

        return Collections.emptyList();
    }

    private boolean getBoolean(@NonNull String key) {
        if (config != null && config.contains(key)) {
            return config.getBoolean(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getBoolean(key);
        }

        return false;
    }

    private double getDouble(@NonNull String key) {
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
    @NonNull
    public String getKey() {
        return key;
    }

    @Override
    @Nullable
    public String getAliases() {
        String aliases = getString("aliases");

        if (aliases == null) {
            return getKey();
        }

        return aliases;
    }

    @Override
    public boolean shouldCancelChatEvent() {
        return getBoolean("cancel-message-event");
    }
}
