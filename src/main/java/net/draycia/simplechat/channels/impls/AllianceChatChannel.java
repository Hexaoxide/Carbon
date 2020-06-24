package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import me.clip.placeholderapi.PlaceholderAPI;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChannelChatEvent;
import net.kyori.text.Component;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.format.TextColor;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class AllianceChatChannel extends SimpleChatChannel {

    AllianceChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, SimpleChat simpleChat) {
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
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "color", "<" + getColor().toString() + ">");
        messageFormat = MiniMessageParser.handlePlaceholders(messageFormat, "message", event.getMessage());

        Component formattedMessage = /*TextUtilsKt.removeEscape(*/MiniMessageParser.parseFormat(messageFormat)/*, '\\')*/;

        try {
            Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Resident target = TownyAPI.getInstance().getDataSource().getResident(onlinePlayer.getName());

                if (!resident.isAlliedWith(target)) {
                    continue;
                }

                if (!canUserSeeMessage(player, onlinePlayer)) {
                    continue;
                }

                TextAdapter.sendMessage(onlinePlayer, formattedMessage);
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
            return;
        }

        System.out.println(LegacyComponentSerializer.INSTANCE.serialize(formattedMessage));

        sendMessageToDiscord(player, message);
        sendMessageToBungee(player, message);
    }

    @Override
    public boolean isAllianceChat() {
        return true;
    }

    public static AllianceChatChannel.Builder allianceBuilder(String name) {
        return new AllianceChatChannel.Builder(name);
    }

    public static class Builder extends SimpleChatChannel.Builder {
        private Builder(String name) {
            super(name);
        }
    }
}
