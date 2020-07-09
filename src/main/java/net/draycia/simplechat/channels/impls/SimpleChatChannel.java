package net.draycia.simplechat.channels.impls;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.events.ChatComponentEvent;
import net.draycia.simplechat.events.ChatFormatEvent;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;

public class SimpleChatChannel extends ChatChannel {

    private TextColor color;
    private Map<String, String> formats;
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
    private boolean firstMatchingGroup;

    private SimpleChat simpleChat;

    private ArrayList<Pattern> itemPatterns = new ArrayList<>();

    private SimpleChatChannel() { }

    SimpleChatChannel(TextColor color, Map<String, String> formats, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, boolean firstMatchingGroup, SimpleChat simpleChat) {
        this.color = color;
        this.formats = formats;
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
        this.firstMatchingGroup = firstMatchingGroup;

        this.simpleChat = simpleChat;

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
        TextComponent formattedMessage = (TextComponent)MiniMessage.get().parse(formatEvent.getFormat(),
                "color", "<color:" + color.toString() + ">",
                "phase", Long.toString(System.currentTimeMillis() % 25),
                "server", simpleChat.getConfig().getString("server-name", "Server"),
                "message", formatEvent.getMessage());

        // Call custom chat event
        ChatComponentEvent componentEvent = new ChatComponentEvent(user, this, formattedMessage, formatEvent.getMessage(), getAudience(user));

        Bukkit.getPluginManager().callEvent(componentEvent);

        // Send message as normal
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ChatUser chatUser = simpleChat.getUserService().wrap(onlinePlayer);

            TextColor userColor = chatUser.getChannelSettings(this).getColor();

            String prefix;

            if (isNationChat()) {
                prefix = simpleChat.getConfig().getString("spy-prefix-nation");
            } else if (isTownChat()) {
                prefix = simpleChat.getConfig().getString("spy-prefix-town");
            } else if (isPartyChat()) {
                prefix = simpleChat.getConfig().getString("spy-prefix-mcmmo");
            } else {
                prefix = simpleChat.getConfig().getString("spy-prefix");
            }

            prefix = processPlaceholders(user, prefix);

            if (userColor == null) {
                if (isUserSpying(user, chatUser)) {
                    prefix = prefix.replace("<color>", "<color:" + getColor() + ">");

                    chatUser.sendMessage(MiniMessage.get().parse(prefix).append(componentEvent.getComponent()));
                } else if (componentEvent.getRecipients().contains(chatUser)) {
                    chatUser.sendMessage(componentEvent.getComponent());
                }
            } else {
                prefix = prefix.replace("<color>", "<color:" + chatUser.getChannelSettings(this).getColor().asHexString() + ">");
                String format = formatEvent.getFormat();

                TextComponent newFormat = (TextComponent)MiniMessage.get().parse(format,
                        "color", "<color:" + userColor.toString() + ">",
                        "phase", Long.toString(System.currentTimeMillis() % 25),
                        "server", simpleChat.getConfig().getString("server-name", "Server"),
                        "message", formatEvent.getMessage());

                if (isUserSpying(user, chatUser)) {
                    newFormat = (TextComponent)MiniMessage.get().parse(prefix).append(newFormat);
                } else if (!componentEvent.getRecipients().contains(chatUser)) {
                    return;
                }

                ChatComponentEvent newEvent = new ChatComponentEvent(user, this, newFormat, formatEvent.getMessage(), Collections.singletonList(chatUser));

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
                if (formats.containsKey(entry.toLowerCase())) {
                    group = entry;
                    break;
                }
            }
        }

        return getFormat(group);
    }

    public boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
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
            if (getDistance() > 0 && targetPlayer.getLocation().distance(sender.asPlayer().getLocation()) > getDistance()) {
                return false;
            }
        }

        if (isIgnorable()) {
            if (target.isIgnoringUser(sender) && !targetPlayer.hasPermission("simplechat.ignoreexempt")) {
                return false;
            }

            if (target.getChannelSettings(this).isIgnored()) {
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
    public String getFormat(String group) {
        return getFormats().getOrDefault(group, getFormats().getOrDefault("default", "<white><%player_displayname%<white>> <message>"));
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

    @Override
    public boolean firstMatchingGroup() {
        return firstMatchingGroup;
    }

    @Override
    public List<Pattern> getItemLinkPatterns() {
        return itemPatterns;
    }

    public static SimpleChatChannel.Builder builder(String name) {
        return new SimpleChatChannel.Builder(name);
    }

    public static class Builder extends ChatChannel.Builder {

        private TextColor color = TextColor.of(255, 255, 255);
        private Map<String, String> formats = new HashMap<>();
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

        private Builder() { }

        Builder(String name) {
            this.name = name.toLowerCase();
        }

        @Override
        public SimpleChatChannel build(SimpleChat simpleChat) {
            return new SimpleChatChannel(color, formats, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, firstMatchingGroup, simpleChat);
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
        public Builder setFormats(Map<String, String> formats) {
            this.formats = formats;

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

        @Override
        public Builder setFirstMatchingGroup(boolean firstMatchingGroup) {
            this.firstMatchingGroup = firstMatchingGroup;

            return this;
        }
    }

}
