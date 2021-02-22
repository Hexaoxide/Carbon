package net.draycia.carbon.bukkit.listeners.contexts;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.events.ChannelSwitchEvent;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class NationContext implements Listener {

  private final static @NonNull String KEY = "towny-nation";

  private final @NonNull CarbonChat carbonChat;

  public NationContext(final @NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    CarbonEvents.register(ReceiverContextEvent.class, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(!this.isInSameNation(event.sender(), event.recipient()));
      }
    });

    CarbonEvents.register(ChannelSwitchEvent.class, event -> {
      if (!(event.channel() instanceof TextChannel)) {
        return;
      }

      final TextChannel channel = (TextChannel) event.channel();
      final Context context = channel.context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        if (!this.isInNation(event.user())) {
          event.failureMessage(this.carbonChat.translations().contextMessages().townyNationNotInNation());
          event.cancelled(true);
        }
      }
    });

    CarbonEvents.register(MessageContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        if (!this.isInNation(event.user())) {
          event.user().sendMessage(this.carbonChat.messageProcessor().processMessage(
            this.carbonChat.translations().contextMessages().townyTownNotInTown()));

          event.cancelled(!this.isInNation(event.user()));
        }
      }
    });
  }

  @EventHandler
  public void onResidentRemove(final TownRemoveResidentEvent event) {
    final PlayerUser user = this.carbonChat.userService().wrap(event.getResident().getUUID());
    final ChatChannel channel = user.selectedChannel();

    if (!(channel instanceof TextChannel)) {
      return;
    }

    final TextChannel textChannel = (TextChannel) channel;
    final Context context = textChannel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean()) {
      user.clearSelectedChannel();
    }
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final PlayerUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final ChatChannel channel = user.selectedChannel();

    if (!(channel instanceof TextChannel)) {
      return;
    }

    final TextChannel textChannel = (TextChannel) channel;
    final Context context = textChannel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean() && !this.isInNation(user)) {
      user.clearSelectedChannel();
    }
  }

  public boolean isInNation(final @NonNull PlayerUser user) {
    try {
      return TownyAPI.getInstance().getDataSource().getResident(user.username()).hasNation();
    } catch (final NotRegisteredException exception) {
      exception.printStackTrace();
    }

    return false;
  }

  public boolean isInSameNation(final @NonNull PlayerUser user1, final @NonNull PlayerUser user2) {
    if (Bukkit.getPlayer(user1.uuid()) == null || Bukkit.getPlayer(user2.uuid()) == null) {
      return false;
    }

    try {
      final Resident resident = TownyAPI.getInstance().getDataSource().getResident(user1.username());
      final Resident target = TownyAPI.getInstance().getDataSource().getResident(user2.username());

      if (resident.hasNation() && target.hasNation()) {
        return resident.getTown().getNation().hasResident(target);
      }
    } catch (final NotRegisteredException exception) {
      exception.printStackTrace();
    }

    return false;
  }

}
