package net.draycia.carbon.channels.contexts.impl;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChannelSwitchEvent;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.event.PostOrders;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class mcMMOContext implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  public mcMMOContext(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    this.carbonChat.contextManager().register("mcmmo-party", context -> {
      if ((context.value() instanceof Boolean) && ((Boolean) context.value())) {
        return this.isInSameParty(context.sender(), context.target());
      }

      return true;
    });

    CarbonEvents.register(ChannelSwitchEvent.class, PostOrders.NORMAL, false, event -> {
      final Object party = event.channel().context("mcmmo-party");

      if ((party instanceof Boolean) && ((Boolean) party)) {
        if (!this.isInParty(event.user())) {
          event.cancelled(true);
          event.failureMessage(carbonChat.getConfig().getString("contexts.mcMMO.cancellation-message"));
        }
      }
    });

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.NORMAL, false, event -> {
      final Object party = event.channel().context("mcmmo-party");

      if ((party instanceof Boolean) && ((Boolean) party)) {
        if (!this.isInParty(event.user())) {
          event.cancelled(true);
        }
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
    final Object party = user.selectedChannel().context("mcmmo-party");

    if ((party instanceof Boolean) && ((Boolean) party)) {
      user.clearSelectedChannel();
    }
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer());
    final Object party = user.selectedChannel().context("mcmmo-party");

    if ((party instanceof Boolean) && ((Boolean) party) && !this.isInParty(user)) {
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
