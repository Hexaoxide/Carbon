package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AllianceChatChannel extends NationChatChannel implements Listener {

    public AllianceChatChannel(String name, SimpleChat simpleChat) {
        super(name, simpleChat);
    }

    @Override
    public boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying) {
        if (checkSpying && target.asPlayer().hasPermission("simplechat.spy." + getName())) {
            if (target.getChannelSettings(this).isSpying()) {
                return true;
            }
        }

        if (super.canPlayerSee(sender, target, false) && sender != null) {
            try {
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(target.asPlayer().getName());
                Resident targetResident = TownyAPI.getInstance().getDataSource().getResident(sender.asPlayer().getName());

                if (resident.isAlliedWith(targetResident)) {
                    return true;
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean isAllianceChat() {
        return true;
    }

    @Override
    public boolean isNationChat() {
        return false;
    }

}
