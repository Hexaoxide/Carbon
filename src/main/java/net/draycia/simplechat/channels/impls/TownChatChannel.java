package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.clip.placeholderapi.PlaceholderAPI;
import me.minidigger.minimessage.text.MiniMessageParser;
import me.minidigger.minimessage.text.MiniMessageSerializer;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChannelChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class TownChatChannel extends SimpleChatChannel {

    TownChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, simpleChat);
    }

    @Override
    public void sendMessage(OfflinePlayer player, String message) {
        message = MiniMessageParser.escapeTokens(message);

        String group;

        if (player.isOnline()) {
            group = getSimpleChat().getPermission().getPrimaryGroup(player.getPlayer());
        } else {
            group = getSimpleChat().getPermission().getPrimaryGroup(null, player);
        }

        String messageFormat = getFormat(group);

        ChannelChatEvent event = new ChannelChatEvent(player, this, messageFormat, message);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        messageFormat = PlaceholderAPI.setPlaceholders(player, event.getFormat());

        // Convert legacy color codes to Mini color codes
        Component component = LegacyComponentSerializer.legacy('&').deserialize(messageFormat);
        messageFormat = MiniMessageSerializer.serialize(component);

        // Continue parsing the format
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "color", "<" + getColor().toString() + ">");
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "message", event.getMessage());

        Component formattedMessage = MiniMessageParser.parseFormat(messageFormat);

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

                    getSimpleChat().getPlatform().player(onlinePlayer).sendMessage(formattedMessage);
                }
            } else {
                // TODO: send "you don't belong to a town" message
                return;
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
            return;
        }

        System.out.println(LegacyComponentSerializer.legacy().serialize(formattedMessage));
    }

    public static TownChatChannel.Builder townBuilder(String name) {
        return new TownChatChannel.Builder(name);
    }

    public static class Builder extends SimpleChatChannel.Builder {
        private Builder(String name) {
            super(name);
        }
    }

}
