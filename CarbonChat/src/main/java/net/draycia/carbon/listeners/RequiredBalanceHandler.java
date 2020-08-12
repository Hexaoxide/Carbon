package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RequiredBalanceHandler implements Listener {
    private final CarbonChat carbonChat;
    private final Economy economy;
    
    public RequiredBalanceHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        this.economy = carbonChat.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }
    
    @EventHandler
    public void onChat(PreChatFormatEvent event) {
        Double cost = (Double) event.getChannel().getContext("vault-cost");
        Double requiredBal = (Double) event.getChannel().getContext("vault-balance");

        if (cost.equals((double) 0) && requiredBal.equals((double) 0)) return;

        Player player = event.getUser().asPlayer();
        if (!economy.has(player, requiredBal)) {
            event.setCancelled(true);
            event.getUser().sendMessage(carbonChat.getAdventureManager()
                    .processMessageWithPapi(player, event.getChannel().getCannotUseMessage()));
            return;
        }

        if (!economy.has(player, cost)) {
            event.setCancelled(true);
            event.getUser().sendMessage(carbonChat.getAdventureManager()
                    .processMessageWithPapi(player, event.getChannel().getCannotUseMessage()));
            return;
        }

        economy.withdrawPlayer(player, cost);
    }
}
