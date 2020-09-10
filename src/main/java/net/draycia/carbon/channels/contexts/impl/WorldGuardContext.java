package net.draycia.carbon.channels.contexts.impl;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public final class WorldGuardContext implements Listener {

  @NonNull
  private static final String KEY = "worldguard-region";

  public WorldGuardContext(@NonNull CarbonChat carbonChat) {
    carbonChat.getContextManager().register(KEY, (context) -> {
      return this.testContext(context.getSender(), context.getTarget(), context.getValue());
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onChannelSwitch(ChannelSwitchEvent event) {
    // TODO: cancellation message
    Object value = event.getChannel().getContext(KEY);

    if ((value instanceof String || value instanceof List) && !isInRegionOrRegions(value, event.getUser())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onChannelMessage(PreChatFormatEvent event) {
    // TODO: cancellation message
    Object value = event.getChannel().getContext(KEY);

    if ((value instanceof String || value instanceof List) && !isInRegionOrRegions(value, event.getUser())) {
      event.setCancelled(true);
    }
  }

  private boolean isInRegionOrRegions(@Nullable Object value, @NonNull ChatUser user) {
    if (value instanceof String) {
      return isInRegion((String) value, user);
    }

    if (value instanceof List) {
      for (String region : ((List<String>) value)) {
        if (isInRegion(region, user)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean testContext(@NonNull ChatUser sender, @NonNull ChatUser target, @Nullable Object value) {
    boolean user1InRegion = false;
    boolean user2InRegion = false;

    if (value instanceof Boolean && (Boolean) value) {
      return isInSameRegion(sender, target);
    } else if (value instanceof String) {
      user1InRegion = isInRegion((String) value, sender);
      user2InRegion = isInRegion((String) value, target);
    } else if (value instanceof List) {
      for (String item : (List<String>) value) {
        if (!user1InRegion) {
          user1InRegion = isInRegion(item, sender);
        }

        if (!user2InRegion) {
          user2InRegion = isInRegion(item, target);
        }
      }
    } else {
      return true;
    }

    return user1InRegion && user2InRegion;
  }

  public boolean isInSameRegion(@NonNull ChatUser user1, @NonNull ChatUser user2) {
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

  public boolean isInRegion(@NonNull String region, @NonNull ChatUser user) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    World world = BukkitAdapter.adapt(user.asPlayer().getWorld());
    ProtectedRegion protection = container.get(world).getRegion(region);

    return isInRegion(protection, user);
  }

  public boolean isInRegion(@NonNull ProtectedRegion region, @NonNull ChatUser user) {
    Location location = BukkitAdapter.adapt(user.asPlayer().getLocation());

    return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

}
