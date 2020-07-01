package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class TownChatChannel extends SimpleChatChannel {

    TownChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, simpleChat);
    }

    @Override
    public boolean canPlayerSee(ChatUser sender, ChatUser target) {
        if (super.canPlayerSee(sender, target) && sender != null) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(target.asPlayer().getName());

                if (resident.hasTown()) {
                    return resident.getTown().hasResident(sender.asPlayer().getName());
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean canPlayerUse(ChatUser user) {
        if (super.canPlayerUse(user)) {
            try {
                return TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName()).hasTown();
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
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
