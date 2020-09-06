package net.draycia.carbon.channels.impls;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class CarbonChatChannel extends ChatChannel {

    private final String key;

    private final CarbonChat carbonChat;
    private final ConfigurationSection config;

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("<message>");

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
    public @NotNull List<ChatUser> audiences() {
        List<ChatUser> audience = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUser playerUser = carbonChat.getUserService().wrap(player);

            if (canPlayerSee(playerUser, true)) {
                audience.add(playerUser);
            }
        }

        return audience;
    }

    private void updateUserNickname(ChatUser user) {
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

    public Component sendMessage(ChatUser user, Collection<ChatUser> recipients, String message, boolean fromRemote) {
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
                    "server", carbonChat.getConfig().getString("server-name", "Server"));


            if (isUserSpying(user, target)) {
                String prefix = processPlaceholders(user, carbonChat.getConfig().getString("spy-prefix"));

                formatComponent = (TextComponent) MiniMessage.get().parse(prefix, "color",
                        targetColor.asHexString()).append(formatComponent);
            }

            TranslatableComponent translation = TranslatableComponent.of("<message>", formatComponent);

            TextComponent messageComponent;

            if (user.isOnline() && user.asPlayer().hasPermission("carbonchat.formatting")) {
                messageComponent = (TextComponent) carbonChat.getAdventureManager().processMessage(formatEvent.getMessage(),
                        "br", "\n",
                        "displayname", displayName,
                        "color", "<" + targetColor.asHexString() + ">",
                        "phase", Long.toString(System.currentTimeMillis() % 25),
                        "server", carbonChat.getConfig().getString("server-name", "Server"));
            } else {
                messageComponent = TextComponent.of(formatEvent.getMessage());
            }

            TranslatableComponent processedComponent = translation.args(messageComponent);

            ChatComponentEvent newEvent = new ChatComponentEvent(user, target, this, processedComponent,
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
                "server", carbonChat.getConfig().getString("server-name", "Server"));

        TextComponent consoleMessage = TextComponent.of(consoleFormatEvent.getMessage());

        TranslatableComponent consoleTranslation = TranslatableComponent.of("<message>", consoleFormat)
                .args(consoleMessage);

        ChatComponentEvent consoleEvent = new ChatComponentEvent(user, null, this, consoleTranslation,
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
    public Component sendMessage(ChatUser user, String message, boolean fromRemote) {
        return this.sendMessage(user, this.audiences(), message, fromRemote);
    }

    public String getFormat(ChatUser user) {
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

    private boolean userHasGroup(ChatUser user, String group) {
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
        for (ChatUser user : audiences()) {
            if (!user.isIgnoringUser(player)) {
                user.sendMessage(component);
            }
        }
    }

    public void sendMessageToBungee(Player player, Component component) {
        carbonChat.getMessageManager().sendMessage("channel-component", player.getUniqueId(), (byteArray) -> {
            byteArray.writeUTF(this.getKey());
            byteArray.writeUTF(carbonChat.getAdventureManager().getAudiences().gsonSerializer().serialize(component));
        });
    }

    @Override
    public Boolean canPlayerSee(ChatUser target, boolean checkSpying) {
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

    public Boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
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
    public TextColor getChannelColor(ChatUser user) {
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

    private String getDefaultFormatName() {
        return getString("default-group");
    }

    @Override
    public Boolean isDefault() {
        if (config != null && config.contains("default")) {
            return config.getBoolean("default");
        }

        return false;
    }

    @Override
    public Boolean isIgnorable() {
        return getBoolean("ignorable");
    }

    @Override
    @Deprecated
    public Boolean shouldBungee() {
        return getBoolean("should-bungee");
    }

    @Override
    public Boolean isCrossServer() {
        return getBoolean("is-cross-server");
    }

    @Override
    public Boolean honorsRecipientList() {
        return getBoolean("honors-recipient-list");
    }

    @Override
    public Boolean permissionGroupMatching() {
        return getBoolean("permission-group-matching");
    }

    @Override
    public List<String> getGroupOverrides() {
        return getStringList("group-overrides");
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
    public String getSwitchFailureMessage() {
        return getString("switch-failure-message");
    }

    @Override
    public String getCannotIgnoreMessage() {
        return getString("cannot-ignore-message");
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

    private List<String> getStringList(String key) {
        if (config != null && config.contains(key)) {
            return config.getStringList(key);
        }

        ConfigurationSection defaultSection = carbonChat.getConfig().getConfigurationSection("default");

        if (defaultSection != null && defaultSection.contains(key)) {
            return defaultSection.getStringList(key);
        }

        return Collections.emptyList();
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

    @Override
    public boolean shouldCancelChatEvent() {
        return getBoolean("cancel-message-event");
    }
}
