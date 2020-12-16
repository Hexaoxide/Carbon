package net.draycia.carbon.bukkit.listeners.contexts;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChannelSwitchEvent;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class mcMMOContext implements Listener {

  private final @NonNull CarbonChat carbonChat;

  private final static @NonNull String KEY = "mcmmo-party";

  public mcMMOContext(final @NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    CarbonEvents.register(ReceiverContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.cancelled(!this.isInSameParty(event.sender(), event.recipient()));
      }
    });

    CarbonEvents.register(ChannelSwitchEvent.class, PostOrders.NORMAL, false, event -> {
      if (!(event.channel() instanceof TextChannel)) {
        return;
      }

      final TextChannel channel = (TextChannel) event.channel();
      final Context context = channel.context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        if (!this.isInParty(event.user())) {
          event.failureMessage(this.carbonChat.translations().contextMessages().mcmmoPartyNotInParty());
          event.cancelled(true);
        }
      }
    });

    CarbonEvents.register(MessageContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context != null && context.isBoolean() && context.asBoolean()) {
        event.user().sendMessage(this.carbonChat.messageProcessor().processMessage(
          this.carbonChat.translations().contextMessages().mcmmoPartyNotInParty()));

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

    this.checkContext(event.getPlayer());
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    this.checkContext(event.getPlayer());
  }

  private void checkContext(final Player player) {
    final PlayerUser user = this.carbonChat.userService().wrap(player.getUniqueId());
    final ChatChannel channel = user.selectedChannel();

    if (!(channel instanceof TextChannel)) {
      return;
    }

    final TextChannel textChannel = (TextChannel) channel;
    final Context context = textChannel.context(KEY);

    if (context != null && context.isBoolean() && context.asBoolean() && !this.isInParty(user)) {
      user.clearSelectedChannel();
    }
  }

  public boolean isInParty(final @NonNull PlayerUser user) {
    final Player player = Bukkit.getPlayer(user.uuid());

    if (player == null) {
      return false;
    }

    return PartyAPI.inParty(player);
  }

  public boolean isInSameParty(final @NonNull PlayerUser user1, final @NonNull PlayerUser user2) {
    final Player player1 = Bukkit.getPlayer(user1.uuid());
    final Player player2 = Bukkit.getPlayer(user2.uuid());

    if (player1 == null || player2 == null) {
      return false;
    }

    return PartyAPI.inSameParty(player1, player2);
  }

}
