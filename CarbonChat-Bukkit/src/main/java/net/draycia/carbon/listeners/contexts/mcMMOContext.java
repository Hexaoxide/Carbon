package net.draycia.carbon.listeners.contexts;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
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

public final class mcMMOContext implements Listener {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  @NonNull
  private static final String KEY = "mcmmo-party";

  public mcMMOContext(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;

    CarbonEvents.register(ReceiverContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        // TODO: failureMessage
        event.cancelled(!this.isInSameParty(event.sender(), event.recipient()));
      }
    });

    CarbonEvents.register(ChannelSwitchEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        if (!this.isInParty(event.user())) {
          event.failureMessage(carbonChat.getConfig().getString("contexts.mcMMO.cancellation-message"));
          event.cancelled(true);
        }
      }
    });

    CarbonEvents.register(MessageContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(!this.isInParty(event.user()));
      }
    });
  }

  private static final McMMOPartyChangeEvent.EventReason LEFT = McMMOPartyChangeEvent.EventReason.LEFT_PARTY;
  private static final McMMOPartyChangeEvent.EventReason KICKED = McMMOPartyChangeEvent.EventReason.KICKED_FROM_PARTY;

  @EventHandler(ignoreCancelled = true)
  public void onPartyLeave(final McMMOPartyChangeEvent event) {
    if (event.getReason() != LEFT && event.getReason() != KICKED) {
      return;
    }

    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      return;
    }

    final Context context = channel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean() && !this.isInParty(user)) {
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

    if (context != null && context.isBoolean() && context.asBoolean() && !this.isInParty(user)) {
      user.clearSelectedChannel();
    }
  }

  public boolean isInParty(final @NonNull ChatUser user) {
    return PartyAPI.inParty(Bukkit.getPlayer(user.uuid()));
  }

  public boolean isInSameParty(final @NonNull ChatUser user1, final @NonNull ChatUser user2) {
    if (Bukkit.getPlayer(user1.uuid()) == null || Bukkit.getPlayer(user2.uuid()) == null) {
      return false;
    }

    return PartyAPI.inSameParty(Bukkit.getPlayer(user1.uuid()), Bukkit.getPlayer(user2.uuid()));
  }

}
