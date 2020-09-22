package net.draycia.carbon.listeners.contexts;

import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.Context;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EconomyContext {
  @NonNull
  private final CarbonChatBukkit carbonChat;

  @NonNull
  private final Economy economy;

  public EconomyContext(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;
    this.economy = this.carbonChat.getServer().getServicesManager().getRegistration(Economy.class).getProvider();

    CarbonEvents.register(PreChatFormatEvent.class, event -> {
      final Context context = event.channel().context("vault-balance");

      if (context == null) {
        return;
      }

      final Double requiredBal;

      if (context.isNumber()) {
        requiredBal = context.asNumber().doubleValue();;
      } else {
        return;
      }

      if (requiredBal.equals(0.0)) {
        return;
      }

      final OfflinePlayer player = Bukkit.getOfflinePlayer(event.user().uuid());

      if (!this.economy.has(player, requiredBal)) {
        event.cancelled(true);

        event.user().sendMessage(this.carbonChat.messageProcessor()
          .processMessage(event.channel().cannotUseMessage()));
      }
    });

    CarbonEvents.register(PreChatFormatEvent.class, event -> {
      final Context context = event.channel().context("vault-cost");

      if (context == null) {
        return;
      }

      final Double cost;

      if (context.isNumber()) {
        cost = context.asNumber().doubleValue();
      } else {
        return;
      }

      if (cost.equals(0.0)) {
        return;
      }

      final OfflinePlayer player = Bukkit.getOfflinePlayer(event.user().uuid());

      if (!this.economy.has(player, cost)) {
        event.cancelled(true);

        event.user().sendMessage(this.carbonChat.messageProcessor()
          .processMessage(event.channel().cannotUseMessage()));

        return;
      }

      this.economy.withdrawPlayer(player, cost);
    });
  }

}
