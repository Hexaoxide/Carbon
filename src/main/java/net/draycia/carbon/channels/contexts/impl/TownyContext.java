package net.draycia.carbon.channels.contexts.impl;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
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

    this.carbonChat.getContextManager().register(KEY, context -> {
      if ((context.value() instanceof Boolean) && ((Boolean) context.value())) {
        return this.isInSameTown(context.sender(), context.target());
      }

      return true;
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onChannelSwitch(final ChannelSwitchEvent event) {
    final Object town = event.channel().context(KEY);

    if ((town instanceof Boolean) && ((Boolean) town)) {
      if (!this.isInTown(event.user())) {
        event.setCancelled(true);
        event.failureMessage(this.carbonChat.getConfig().getString("contexts.Towny.cancellation-message"));
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onChannelMessage(final PreChatFormatEvent event) {
    // TODO: event.setFailureMessage
    final Object town = event.channel().context(KEY);

    if ((town instanceof Boolean) && ((Boolean) town)) {
      if (!this.isInTown(event.user())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onResidentRemove(final TownRemoveResidentEvent event) {
    final String name = event.getResident().getName();
    final ChatUser user = this.carbonChat.getUserService().wrap(name);
    final Object town = user.selectedChannel().context(KEY);

    if ((town instanceof Boolean) && ((Boolean) town)) {
      user.clearSelectedChannel();
    }
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final ChatUser user = this.carbonChat.getUserService().wrap(event.getPlayer());
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
