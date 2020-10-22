package net.draycia.carbon.bukkit.listeners.contexts;

import net.draycia.carbon.bukkit.CarbonChatBukkit;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.Context;
import net.kyori.adventure.identity.Identity;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EconomyContext {
  private final @NonNull CarbonChatBukkit carbonChat;

  private final @NonNull Economy economy;

  public EconomyContext(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;

    final RegisteredServiceProvider<Economy> provider =
      this.carbonChat.getServer().getServicesManager().getRegistration(Economy.class);

    if (provider == null) {
      throw new IllegalArgumentException("Economy provider not found!");
    }

    this.economy = provider.getProvider();

    CarbonEvents.register(PreChatFormatEvent.class, event -> {
      if (!(event.channel() instanceof TextChannel)) {
        return;
      }

      final TextChannel channel = (TextChannel) event.channel();
      final Context context = channel.context("vault-balance");

      if (context == null) {
        return;
      }

      final Double requiredBal;

      if (context.isNumber()) {
        requiredBal = context.asNumber().doubleValue();
      } else {
        return;
      }

      if (requiredBal.equals(0.0)) {
        return;
      }

      final OfflinePlayer player = Bukkit.getOfflinePlayer(event.user().uuid());

      if (!this.economy.has(player, requiredBal)) {
        event.cancelled(true);

        event.user().sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
          .processMessage(this.carbonChat.translations().contextMessages().vaultBalanceNotEnough()));
      }
    });

    CarbonEvents.register(PreChatFormatEvent.class, event -> {
      if (!(event.channel() instanceof TextChannel)) {
        return;
      }

      final TextChannel channel = (TextChannel) event.channel();
      final Context context = channel.context("vault-cost");

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

        event.user().sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
          .processMessage(this.carbonChat.translations().contextMessages().vaultCostNotEnough()));

        return;
      }

      this.economy.withdrawPlayer(player, cost);
    });
  }

}
