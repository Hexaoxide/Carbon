package net.draycia.carbon.channels.contexts.impl;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EconomyContext implements Listener {
    private final @NonNull CarbonChat carbonChat;
    private final @NonNull Economy economy;

    public EconomyContext(@NonNull CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        this.economy = carbonChat.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }
    
    @EventHandler
    public void onChatReqBal(@NonNull PreChatFormatEvent event) {
        Object requiredBalObject = event.getChannel().getContext("vault-balance");

        Double requiredBal;

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

        Player player = event.getUser().asPlayer();

        if (!economy.has(player, requiredBal)) {
            event.setCancelled(true);

            event.getUser().sendMessage(carbonChat.getAdventureManager()
                    .processMessageWithPapi(player, event.getChannel().getCannotUseMessage()));
        }

    }

    @EventHandler
    public void onChatCost(@NonNull PreChatFormatEvent event) {
        Object costObject = event.getChannel().getContext("vault-cost");

        Double cost;

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

        Player player = event.getUser().asPlayer();

        if (!economy.has(player, cost)) {
            event.setCancelled(true);

            event.getUser().sendMessage(carbonChat.getAdventureManager()
                    .processMessageWithPapi(player, event.getChannel().getCannotUseMessage()));

            return;
        }

        economy.withdrawPlayer(player, cost);
    }
}
