package net.draycia.carbon.listeners.contexts;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChannelContextEvent;
import net.draycia.carbon.events.api.MessageContextEvent;
import net.draycia.carbon.events.api.ReceiverContextEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.Context;
import net.kyori.event.PostOrders;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class mcMMOContext implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private static final String KEY = "mcmmo-party";

  public mcMMOContext(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    CarbonEvents.register(ReceiverContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(this.isInSameParty(event.sender(), event.recipient()));
      }
    });

    CarbonEvents.register(ChannelContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(this.isInParty(event.user()));
      }
    });

    CarbonEvents.register(MessageContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(this.isInParty(event.user()));
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

    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer());
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
    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer());
    final ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      return;
    }

    final Context context = channel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean() && !this.isInParty(user)) {
      user.clearSelectedChannel();
    }
  }

  public boolean isInParty(@NonNull final ChatUser user) {
    return PartyAPI.inParty(user.player());
  }

  public boolean isInSameParty(@NonNull final ChatUser user1, @NonNull final ChatUser user2) {
    if (!user1.online() || !user2.online()) {
      return false;
    }

    return PartyAPI.inSameParty(user1.player(), user2.player());
  }

}
