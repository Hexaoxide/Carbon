package net.draycia.simplechatworldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class SimpleChatWorldGuard extends JavaPlugin {

    private SimpleChat simpleChat;

    @Override
    public void onEnable() {
        simpleChat = (SimpleChat) Bukkit.getPluginManager().getPlugin("SimpleChat");

        simpleChat.getContextManager().register("worldguard-region", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameRegion(context.getSender(), context.getTarget());
            }

            return true;
        });
    }

    public boolean  isInSameRegion(ChatUser user1, ChatUser user2) {
        if (!user1.isOnline() || !user2.isOnline()) {
            return false;
        }

        Location user1Location = BukkitAdapter.adapt(user1.asPlayer().getLocation());
        Location user2Location = BukkitAdapter.adapt(user2.asPlayer().getLocation());

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = container.createQuery();

        ApplicableRegionSet user1Regions = regionQuery.getApplicableRegions(user1Location);
        ApplicableRegionSet user2Regions = regionQuery.getApplicableRegions(user2Location);

        for (ProtectedRegion region : user1Regions) {
            if (user2Regions.getRegions().contains(region)) {
                return true;
            }
        }

        return false;
    }

}
