package net.draycia.carbon.listeners.contexts;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChannelContextEvent;
import net.draycia.carbon.events.api.MessageContextEvent;
import net.draycia.carbon.events.api.ReceiverContextEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.Context;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class WorldGuardContext {

  @NonNull
  private static final String KEY = "worldguard-region";

  public WorldGuardContext() {
    CarbonEvents.register(ReceiverContextEvent.class, event -> {
      event.cancelled(this.testContext(event.sender(), event.recipient(), event.context(KEY)));
    });

    CarbonEvents.register(ChannelContextEvent.class, event -> {
      final Context value = event.channel().context(KEY);

      if (value != null && (value.isString() || value.isList()) && !this.isInRegionOrRegions(value, event.user())) {
        event.cancelled(true);
      }
    });

    CarbonEvents.register(MessageContextEvent.class, event -> {
      final Context value = event.channel().context(KEY);

      if (value != null && (value.isString() || value.isList()) && !this.isInRegionOrRegions(value, event.user())) {
        event.cancelled(true);
      }
    });
  }

  private boolean isInRegionOrRegions(@NonNull final Context context, @NonNull final ChatUser user) {
    if (context.isString()) {
      return this.isInRegion(context.asString(), user);
    }

    if (context.isList()) {
      for (final String region : context.asStringList()) {
        if (this.isInRegion(region, user)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean testContext(@NonNull final ChatUser sender, @NonNull final ChatUser target, @NonNull final Context context) {
    boolean user1InRegion = false;
    boolean user2InRegion = false;

    if (context.isBoolean() && context.asBoolean()) {
      return this.isInSameRegion(sender, target);
    } else if (context.isString()) {
      user1InRegion = this.isInRegion(context.asString(), sender);
      user2InRegion = this.isInRegion(context.asString(), target);
    } else if (context.isList()) {
      for (final String item : context.asStringList()) {
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
