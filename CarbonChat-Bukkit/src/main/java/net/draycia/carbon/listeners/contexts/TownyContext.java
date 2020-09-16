package net.draycia.carbon.listeners.contexts;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChannelSwitchEvent;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.Context;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class TownyContext implements Listener {

  @NonNull
  private static final String KEY = "towny-town";

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public TownyContext(@NonNull final CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;

    CarbonEvents.register(ReceiverContextEvent.class, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(!this.isInSameTown(event.sender(), event.recipient()));
      }
    });

    CarbonEvents.register(ChannelSwitchEvent.class, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        if (!this.isInTown(event.user())) {
          event.failureMessage(carbonChat.getConfig().getString("contexts.Towny.cancellation-message"));
          event.cancelled(true);
        }
      }
    });

    CarbonEvents.register(MessageContextEvent.class, PostOrders.NORMAL, false, event -> {
      // TODO: event.setFailureMessage
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        if (!this.isInTown(event.user())) {
          event.cancelled(!this.isInTown(event.user()));
        }
      }
    });
  }

  @EventHandler
  public void onResidentRemove(final TownRemoveResidentEvent event) {
    final String name = event.getResident().getName();
    final ChatUser user = this.carbonChat.userService().wrap(Bukkit.getPlayer(name).getUniqueId());
    final ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      return;
    }

    final Context context = channel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean()) {
      user.clearSelectedChannel();
    }
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      return;
    }

    final Context context = channel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean() && !this.isInTown(user)) {
      user.clearSelectedChannel();
    }
  }

  public boolean isInTown(@NonNull final ChatUser user) {
    try {
      return TownyAPI.getInstance().getDataSource().getResident(Bukkit.getPlayer(user.uuid()).getName()).hasTown();
    } catch (final NotRegisteredException exception) {
      exception.printStackTrace();
    }

    return false;
  }

  public boolean isInSameTown(@NonNull final ChatUser user1, @NonNull final ChatUser user2) {
    if (Bukkit.getPlayer(user1.uuid()) == null || Bukkit.getPlayer(user2.uuid()) == null) {
      return false;
    }

    try {
      final Resident resident = TownyAPI.getInstance().getDataSource().getResident(Bukkit.getPlayer(user1.uuid()).getName());

      if (resident.hasTown()) {
        return resident.getTown().hasResident(Bukkit.getPlayer(user2.uuid()).getName());
      }
    } catch (final NotRegisteredException exception) {
      exception.printStackTrace();
    }

    return false;
  }

}
