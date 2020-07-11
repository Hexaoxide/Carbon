package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NationChatChannel extends SimpleChatChannel implements Listener {

    public NationChatChannel(String name, SimpleChat simpleChat) {
        super(name, simpleChat);
    }

    @Override
    public String processPlaceholders(ChatUser user, String input) {
        return input.replace("<nation>", getNation(user));
    }

    private String getNation(ChatUser user) {
        try {
            Resident resident = TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName());

            if (resident.hasNation()) {
                return resident.getTown().getNation().getFormattedName();
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        return "";
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
                Resident resident = TownyAPI.getInstance().getDataSource().getResident(sender.asPlayer().getName());

                if (resident.hasNation()) {
                    Nation nation = resident.getTown().getNation();

                    if (nation.hasResident(target.asPlayer().getName())) {
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
    public boolean canPlayerUse(ChatUser user) {
        return super.canPlayerUse(user) && isInNation(user);
    }

    public boolean isInNation(ChatUser user) {
        try {
            return TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName()).hasNation();
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        return false;
    }

    @EventHandler
    public void onResidentRemove(TownRemoveResidentEvent event) {
        String name = event.getResident().getName();
        ChatUser user = getSimpleChat().getUserService().wrap(Bukkit.getOfflinePlayer(name));

        if (user.getSelectedChannel().isAllianceChat()) {
            user.clearSelectedChannel();
        }
    }

    @EventHandler
    public void onNationRemoveTown(NationRemoveTownEvent event) {
        for (Resident resident : event.getTown().getResidents()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resident.getName());

            if (!offlinePlayer.isOnline()) {
                continue;
            }

            ChatUser user = getSimpleChat().getUserService().wrap(offlinePlayer);

            if (user.getSelectedChannel().isAllianceChat()) {
                user.clearSelectedChannel();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = getSimpleChat().getUserService().wrap(event.getPlayer());

        if (user.getSelectedChannel().isAllianceChat() && !isInNation(user)) {
            user.clearSelectedChannel();
        }
    }

    @Override
    public boolean isNationChat() {
        return true;
    }

}
