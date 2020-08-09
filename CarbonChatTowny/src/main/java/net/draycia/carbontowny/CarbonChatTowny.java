package net.draycia.carbontowny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CarbonChatTowny extends JavaPlugin {

    private CarbonChat carbonChat;
    private static final String KEY = "towny-town";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

        carbonChat.getContextManager().register("towny-town", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameTown(context.getSender(), context.getTarget());
            }

            return true;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onChannelSwitch(ChannelSwitchEvent event) {
        Object town = event.getChannel().getContext(KEY);

        if ((town instanceof Boolean) && ((Boolean) town)) {
            if (!isInTown(event.getUser())) {
                event.setCancelled(true);
                event.setFailureMessage(getConfig().getString("cancellation-message"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChannelMessage(ChatFormatEvent event) {
        // TODO: event.setFailureMessage
        Object town = event.getChannel().getContext(KEY);

        if ((town instanceof Boolean) && ((Boolean) town)) {
            if (!isInTown(event.getUser())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onResidentRemove(TownRemoveResidentEvent event) {
        String name = event.getResident().getName();
        ChatUser user = carbonChat.getUserService().wrap(name);
        Object town = user.getSelectedChannel().getContext(KEY);

        if ((town instanceof Boolean) && ((Boolean) town)) {
            user.clearSelectedChannel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());
        Object town = user.getSelectedChannel().getContext(KEY);

        if ((town instanceof Boolean) && ((Boolean) town) && !isInTown(user)) {
            user.clearSelectedChannel();
        }
    }

    public boolean isInTown(ChatUser user) {
        try {
            return TownyAPI.getInstance().getDataSource().getResident(user.asPlayer().getName()).hasTown();
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isInSameTown(ChatUser user1, ChatUser user2) {
        if (!user1.isOnline() || !user2.isOnline()) {
            return false;
        }

        try {
            Resident resident = TownyAPI.getInstance().getDataSource().getResident(user1.asPlayer().getName());

            if (resident.hasTown()) {
                return resident.getTown().hasResident(user2.asPlayer().getName());
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        return false;
    }

}
