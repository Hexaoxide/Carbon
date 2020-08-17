package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RequiredBalanceHandler implements Listener {
    private final CarbonChat carbonChat;
    private final Economy economy;
    private final boolean enabled;
    
    public RequiredBalanceHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        RegisteredServiceProvider<Economy> registeredEco = carbonChat.getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredEco == null) {
            carbonChat.getServer().getLogger().warning("No Vault-compatible Economy plugin detected! Economy features won't work!");

            this.economy = null;
            this.enabled = false;

            return;
        }

        this.enabled = true;
        this.economy = registeredEco.getProvider();
    }
    
    @EventHandler
    public void onChatReqBal(PreChatFormatEvent event) {
        if (!enabled) {
            return;
        }

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
    public void onChatCost(PreChatFormatEvent event) {
        if (!enabled) {
            return;
        }

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
