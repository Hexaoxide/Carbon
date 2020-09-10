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

  public WorldGuardContext(@NonNull final CarbonChat carbonChat) {
    carbonChat.contextManager().register(KEY, context -> {
      return this.testContext(context.sender(), context.target(), context.value());
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onChannelSwitch(final ChannelSwitchEvent event) {
    // TODO: cancellation message
    final Object value = event.channel().context(KEY);

    if ((value instanceof String || value instanceof List) && !this.isInRegionOrRegions(value, event.user())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onChannelMessage(final PreChatFormatEvent event) {
    // TODO: cancellation message
    final Object value = event.channel().context(KEY);

    if ((value instanceof String || value instanceof List) && !this.isInRegionOrRegions(value, event.user())) {
      event.setCancelled(true);
    }
  }

  private boolean isInRegionOrRegions(@Nullable final Object value, @NonNull final ChatUser user) {
    if (value instanceof String) {
      return this.isInRegion((String) value, user);
    }

    if (value instanceof List) {
      for (final String region : (List<String>) value) {
        if (this.isInRegion(region, user)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean testContext(@NonNull final ChatUser sender, @NonNull final ChatUser target, @Nullable final Object value) {
    boolean user1InRegion = false;
    boolean user2InRegion = false;

    if (value instanceof Boolean && (Boolean) value) {
      return this.isInSameRegion(sender, target);
    } else if (value instanceof String) {
      user1InRegion = this.isInRegion((String) value, sender);
      user2InRegion = this.isInRegion((String) value, target);
    } else if (value instanceof List) {
      for (final String item : (List<String>) value) {
        if (!user1InRegion) {
          user1InRegion = this.isInRegion(item, sender);
        }

        if (!user2InRegion) {
          user2InRegion = this.isInRegion(item, target);
        }
      }
    } else {
      return true;
    }

    return user1InRegion && user2InRegion;
  }

  public boolean isInSameRegion(@NonNull final ChatUser user1, @NonNull final ChatUser user2) {
    if (!user1.online() || !user2.online()) {
      return false;
    }

    final Location user1Location = BukkitAdapter.adapt(user1.player().getLocation());
    final Location user2Location = BukkitAdapter.adapt(user2.player().getLocation());

    final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    final RegionQuery regionQuery = container.createQuery();

    final ApplicableRegionSet user1Regions = regionQuery.getApplicableRegions(user1Location);
    final ApplicableRegionSet user2Regions = regionQuery.getApplicableRegions(user2Location);

    for (final ProtectedRegion region : user1Regions) {
      if (user2Regions.getRegions().contains(region)) {
        return true;
      }
    }

    return false;
  }

  public boolean isInRegion(@NonNull final String region, @NonNull final ChatUser user) {
    final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    final World world = BukkitAdapter.adapt(user.player().getWorld());
    final ProtectedRegion protection = container.get(world).getRegion(region);

    return this.isInRegion(protection, user);
  }

  public boolean isInRegion(@NonNull final ProtectedRegion region, @NonNull final ChatUser user) {
    final org.bukkit.Location location = user.player().getLocation();

    return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

}
