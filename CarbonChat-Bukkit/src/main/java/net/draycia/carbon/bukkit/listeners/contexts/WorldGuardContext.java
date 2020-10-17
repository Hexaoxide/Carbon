package net.draycia.carbon.bukkit.listeners.contexts;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.Context;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class WorldGuardContext {

  private @NonNull static final String KEY = "worldguard-region";

  public WorldGuardContext() {
    CarbonEvents.register(ReceiverContextEvent.class, event -> {
      final Player senderPlayer = Bukkit.getPlayer(event.sender().uuid());
      final Player recipientPlayer = Bukkit.getPlayer(event.recipient().uuid());

      if (senderPlayer == null || recipientPlayer == null) {
        return;
      }

      event.cancelled(this.testContext(senderPlayer, recipientPlayer, event.context(KEY)));
    });

    CarbonEvents.register(MessageContextEvent.class, event -> {
      final Context value = event.channel().context(KEY);
      final Player player = Bukkit.getPlayer(event.user().uuid());

      if (player == null) {
        return;
      }

      if (value != null && (value.isString() || value.isList()) && !this.isInRegionOrRegions(value, player)) {
        event.cancelled(true);
      }
    });
  }

  private boolean isInRegionOrRegions(@NonNull final Context context, @NonNull final Player player) {
    if (context.isString()) {
      return this.isInRegion(context.asString(), player);
    }

    if (context.isList()) {
      for (final String region : context.<String>asList()) {
        if (this.isInRegion(region, player)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean testContext(@NonNull final Player sender, @NonNull final Player target, @NonNull final Context context) {
    boolean user1InRegion = false;
    boolean user2InRegion = false;

    if (context.isBoolean() && context.asBoolean()) {
      return this.isInSameRegion(sender, target);
    } else if (context.isString()) {
      user1InRegion = this.isInRegion(context.asString(), sender);
      user2InRegion = this.isInRegion(context.asString(), target);
    } else if (context.isList()) {
      for (final String item : context.<String>asList()) {
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

  public boolean isInSameRegion(@NonNull final Player user1, @NonNull final Player user2) {
    final Location user1Location = BukkitAdapter.adapt(user1.getLocation());
    final Location user2Location = BukkitAdapter.adapt(user2.getLocation());

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

  public boolean isInRegion(@NonNull final String region, @NonNull final Player player) {
    final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    final World world = BukkitAdapter.adapt(player.getWorld());
    final ProtectedRegion protection = container.get(world).getRegion(region);

    return this.isInRegion(protection, player);
  }

  public boolean isInRegion(@NonNull final ProtectedRegion region, @NonNull final Player player) {
    final org.bukkit.Location location = player.getLocation();

    return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

}
