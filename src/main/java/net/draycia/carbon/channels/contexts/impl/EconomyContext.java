package net.draycia.carbon.channels.contexts.impl;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EconomyContext implements Listener {
  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final Economy economy;

  public EconomyContext(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.economy = this.carbonChat.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
  }

  @EventHandler
  public void onChatReqBal(final PreChatFormatEvent event) {
    final Object requiredBalObject = event.channel().context("vault-balance");

    final Double requiredBal;

    if (requiredBalObject instanceof Double) {
      requiredBal = (Double) requiredBalObject;
    } else if (requiredBalObject instanceof Integer) {
      requiredBal = Double.valueOf((Integer) requiredBalObject);
    } else {
      return;
    }

    if (requiredBal.equals(0.0)) {
      return;
    }

    final Player player = event.user().player();

    if (!this.economy.has(player, requiredBal)) {
      event.setCancelled(true);

      event.user().sendMessage(this.carbonChat.adventureManager()
        .processMessageWithPapi(player, event.channel().cannotUseMessage()));
    }

  }

  @EventHandler
  public void onChatCost(final PreChatFormatEvent event) {
    final Object costObject = event.channel().context("vault-cost");

    final Double cost;

    if (costObject instanceof Double) {
      cost = (Double) costObject;
    } else if (costObject instanceof Integer) {
      cost = Double.valueOf((Integer) costObject);
    } else {
      return;
    }

    if (cost.equals(0.0)) {
      return;
    }

    final Player player = event.user().player();

    if (!this.economy.has(player, cost)) {
      event.setCancelled(true);

      event.user().sendMessage(this.carbonChat.adventureManager()
        .processMessageWithPapi(player, event.channel().cannotUseMessage()));

      return;
    }

    this.economy.withdrawPlayer(player, cost);
  }
}
