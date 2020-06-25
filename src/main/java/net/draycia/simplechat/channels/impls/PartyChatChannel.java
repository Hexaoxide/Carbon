package net.draycia.simplechat.channels.impls;

import com.gmail.nossr50.api.PartyAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChannelChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class PartyChatChannel extends SimpleChatChannel {

    PartyChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, simpleChat);
    }

    @Override
    public void sendMessage(OfflinePlayer player, String message) {
        message = MiniMessageParser.escapeTokens(message);

        String group;

        if (player.isOnline()) {
            group = getSimpleChat().getPermission().getPrimaryGroup(player.getPlayer());
        } else {
            return;
        }

        String messageFormat = getFormat(group);

        ChannelChatEvent event = new ChannelChatEvent(player, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        messageFormat = PlaceholderAPI.setPlaceholders(player, event.getFormat());
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "color", "<" + getColor().toString() + ">");
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "message", event.getMessage());

        Component formattedMessage = /*TextUtilsKt.removeEscape(*/MiniMessageParser.parseFormat(messageFormat)/*, '\\')*/;

        if (PartyAPI.inParty((Player)player)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!PartyAPI.inSameParty((Player)player, onlinePlayer)) {
                    continue;
                }

                if (!canUserSeeMessage(player, onlinePlayer)) {
                    continue;
                }

                getSimpleChat().getPlatform().player(onlinePlayer).sendMessage(formattedMessage);
            }
        } else {
            // TODO: send "you don't belong to a party" message
            return;
        }

        System.out.println(LegacyComponentSerializer.legacy().serialize(formattedMessage));
    }

    public static PartyChatChannel.Builder partyBuilder(String name) {
        return new PartyChatChannel.Builder(name);
    }

    public static class Builder extends SimpleChatChannel.Builder {
        private Builder(String name) {
            super(name);
        }
    }

}
