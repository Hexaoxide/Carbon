package net.draycia.carbonworldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class CarbonChatWorldGuard extends JavaPlugin {

    @Override
    public void onEnable() {
        CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

        carbonChat.getContextManager().register("worldguard-region", (context) -> {
            if ((context.getValue() instanceof Boolean) && ((Boolean) context.getValue())) {
                return isInSameRegion(context.getSender(), context.getTarget());
            }

            boolean user1InRegion = false;
            boolean user2InRegion = false;

            if (context.getValue() instanceof String) {
                user1InRegion = isInRegion((String)context.getValue(), context.getSender());
                user2InRegion = isInRegion((String)context.getValue(), context.getTarget());
            } else if (context.getValue() instanceof List) {
                for (String item : (List<String>) context.getValue()) {
                    if (!user1InRegion) {
                        user1InRegion = isInRegion(item, context.getSender());
                    }

                    if (!user2InRegion) {
                        user2InRegion = isInRegion(item, context.getTarget());
                    }
                }
            }

            return user1InRegion && user2InRegion;
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

    public boolean isInRegion(String region, ChatUser user) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(user.asPlayer().getWorld());
        ProtectedRegion protection = container.get(world).getRegion(region);

        return isInRegion(protection, user);
    }

    public boolean isInRegion(ProtectedRegion region, ChatUser user) {
        Location location = BukkitAdapter.adapt(user.asPlayer().getLocation());

        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
