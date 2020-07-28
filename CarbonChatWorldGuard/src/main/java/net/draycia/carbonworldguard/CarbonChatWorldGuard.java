package net.draycia.carbonworldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CarbonChatWorldGuard extends JavaPlugin {

    @Override
    public void onEnable() {
        CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

        carbonChat.getContextManager().register("worldguard-region", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameRegion(context.getSender(), context.getTarget());
            }

            if (context.getValue() instanceof String) {
                return isInSameRegion((String) context.getValue(), context.getSender(), context.getTarget());
            }

            return true;
        });
    }

    public boolean isInSameRegion(ChatUser user1, ChatUser user2) {
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

    public boolean isInSameRegion(String region, ChatUser user1, ChatUser user2) {
        if (!user1.isOnline() || !user2.isOnline()) {
            return false;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(user1.asPlayer().getWorld()));

        if (regionManager == null) {
            return false;
        }

        ProtectedRegion protection = regionManager.getRegion(region);

        if (protection == null) {
            return false;
        }

        if (!isInRegion(protection, user1)) {
            return false;
        }

        return isInRegion(protection, user2);
    }

    public boolean isInRegion(ProtectedRegion region, ChatUser user) {
        Location location = BukkitAdapter.adapt(user.asPlayer().getLocation());

        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
