package net.draycia.carbon.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CarbonPlaceholders extends PlaceholderExpansion {

    private final CarbonChat carbonChat;

    public CarbonPlaceholders(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "carbonchat";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Draycia (Vicarious#0001)";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String onPlaceholderRequest(final Player player, final String identifier) {
        String key = identifier.toLowerCase();

        if (key.startsWith("can_use_")) {
            String value = key.replace("can_use_", "");

            ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(value);

            if (channel == null) {
                return "false";
            }

            ChatUser user = carbonChat.getUserService().wrap(player);

            return channel.canPlayerUse(user) ? "true" : "false";
        } else if (key.startsWith("can_see_")) {
            String value = key.replace("can_see_", "");

            ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(value);

            if (channel == null) {
                return "false";
            }

            ChatUser user = carbonChat.getUserService().wrap(player);

            return channel.canPlayerSee(user, true) ? "true" : "false";
        } else if (key.startsWith("ignoring_channel_")) {
            String value = key.replace("ignoring_channel_", "");

            ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(value);

            if (channel == null) {
                return "false";
            }

            ChatUser user = carbonChat.getUserService().wrap(player);

            return user.getChannelSettings(channel).isIgnored() ? "true" : "false";
        } else if (key.startsWith("selected_channel")) {
            ChatChannel channel = carbonChat.getUserService().wrap(player).getSelectedChannel();

            return channel == null ? carbonChat.getChannelManager().getDefaultChannel().getName() : channel.getName();
        }

        return null;
    }

}
