package net.draycia.carbon.channels.contexts.impl;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChannelSwitchEvent;
import net.draycia.carbon.events.api.ChatComponentEvent;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class TownyContext implements Listener {

  @NonNull
  private static final String KEY = "towny-town";

  @NonNull
  private final CarbonChat carbonChat;

  public TownyContext(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    this.carbonChat.contextManager().register(KEY, context -> {
      if ((context.value() instanceof Boolean) && ((Boolean) context.value())) {
        return this.isInSameTown(context.sender(), context.target());
      }

      return true;
    });

    final TownyContext instance = this;

    CarbonEvents.register(ChannelSwitchEvent.class, event -> {
      final Object town = event.channel().context(KEY);

      if ((town instanceof Boolean) && ((Boolean) town)) {
        if (!this.isInTown(event.user())) {
          event.cancelled(true);
          event.failureMessage(this.carbonChat.getConfig().getString("contexts.Towny.cancellation-message"));
        }
      }
    });

    CarbonEvents.register(PreChatFormatEvent.class, new EventSubscriber<PreChatFormatEvent>() {
      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final PreChatFormatEvent event) {
        // TODO: event.setFailureMessage
        final Object town = event.channel().context(KEY);

        if ((town instanceof Boolean) && ((Boolean) town)) {
          if (!instance.isInTown(event.user())) {
            event.cancelled(true);
          }
        }
      }
    });
  }

  @EventHandler
  public void onResidentRemove(final TownRemoveResidentEvent event) {
    final String name = event.getResident().getName();
    final ChatUser user = this.carbonChat.userService().wrap(name);
    final Object town = user.selectedChannel().context(KEY);

    if ((town instanceof Boolean) && ((Boolean) town)) {
      user.clearSelectedChannel();
    }
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer());
    final Object town = user.selectedChannel().context(KEY);

    if ((town instanceof Boolean) && ((Boolean) town) && !this.isInTown(user)) {
      user.clearSelectedChannel();
    }
  }

  public boolean isInTown(@NonNull final ChatUser user) {
    try {
      return TownyAPI.getInstance().getDataSource().getResident(user.player().getName()).hasTown();
    } catch (final NotRegisteredException exception) {
      exception.printStackTrace();
    }

    return false;
  }

  public boolean isInSameTown(@NonNull final ChatUser user1, @NonNull final ChatUser user2) {
    if (!user1.online() || !user2.online()) {
      return false;
    }

    try {
      final Resident resident = TownyAPI.getInstance().getDataSource().getResident(user1.player().getName());

      if (resident.hasTown()) {
        return resident.getTown().hasResident(user2.player().getName());
      }
    } catch (final NotRegisteredException exception) {
      exception.printStackTrace();
    }

    return false;
  }

}
