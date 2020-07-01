package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class NationChatChannel extends SimpleChatChannel {

    NationChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, boolean shouldBungee, boolean filterEnabled, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, shouldBungee, filterEnabled, simpleChat);
    }

    @Override
    public boolean canPlayerSee(OfflinePlayer offlinePlayer, Player player) {
        if (super.canPlayerSee(offlinePlayer, player) && offlinePlayer != null) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(offlinePlayer.getName());

                if (resident.hasNation()) {
                    Nation nation = resident.getTown().getNation();

                    if (nation.hasResident(player.getName())) {
                        return true;
                    }
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean canPlayerUse(Player player) {
        if (super.canPlayerUse(player)) {
            try {
                return TownyAPI.getInstance().getDataSource().getResident(player.getName()).hasNation();
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static NationChatChannel.Builder nationBuilder(String name) {
        return new NationChatChannel.Builder(name);
    }

    public static class Builder extends SimpleChatChannel.Builder {
        private Builder(String name) {
            super(name);
        }
    }

}
