package net.draycia.simplechat.managers.luckperms;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.managers.luckperms.contexts.CanSeeChannelCalculator;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LuckPermsHookManager {

    private final SimpleChat simpleChat;
    private final ContextManager contextManager;
    private final List<ContextCalculator<Player>> registeredCalculators = new ArrayList<>();

    public LuckPermsHookManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        LuckPerms luckPerms = this.simpleChat.getServer().getServicesManager().load(LuckPerms.class);

        if (luckPerms == null) {
            throw new IllegalStateException("LuckPerms API not loaded.");
        }

        this.contextManager = luckPerms.getContextManager();

        register(() -> new CanSeeChannelCalculator(simpleChat));
    }

    private void register(Supplier<ContextCalculator<Player>> calculatorSupplier) {
        ContextCalculator<Player> calculator = calculatorSupplier.get();

        this.contextManager.registerCalculator(calculator);
        this.registeredCalculators.add(calculator);
    }

    public void unregisterContexts() {
        this.registeredCalculators.forEach(this.contextManager::unregisterCalculator);
        this.registeredCalculators.clear();
    }

}
