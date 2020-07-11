package net.draycia.simplechat.channels.impls;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TownChatChannel extends SimpleChatChannel implements Listener {

    public TownChatChannel(String name, SimpleChat simpleChat) {
        super(name, simpleChat);
    }

    @Override
    public String processPlaceholders(ChatUser user, String input) {
        return input.replace("<town>", getTown(user));
    }

    private String getTown(ChatUser user) {
        try {
            Resident resident = TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName());

            if (resident.hasTown()) {
                return resident.getTown().getFormattedName();
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

    public boolean isInTown(ChatUser user) {
        try {
            return TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName()).hasTown();
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        return false;
    }

    @EventHandler
    public void onResidentRemove(TownRemoveResidentEvent event) {
        String name = event.getResident().getName();
        ChatUser user = getSimpleChat().getUserService().wrap(Bukkit.getOfflinePlayer(name));

        if (user.getSelectedChannel().isTownChat()) {
            user.clearSelectedChannel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = getSimpleChat().getUserService().wrap(event.getPlayer());

        if (user.getSelectedChannel().isTownChat() && !isInTown(user)) {
            user.clearSelectedChannel();
        }
    }

    @Override
    public boolean isTownChat() {
        return true;
    }

}
