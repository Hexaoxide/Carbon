package net.draycia.carbon.paper.integration.parties;

import com.google.inject.Inject;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.integration.Integration;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@DefaultQualifier(NonNull.class)
public class PartiesIntegration implements Integration {

    private final CarbonChannelRegistry channelRegistry;
    private final ConfigManager configManager;
    private final Logger logger;
    private final PartiesIntegration.Config config;

    @Inject
    public PartiesIntegration(
        final CarbonChannelRegistry channelRegistry,
        final ConfigManager configManager,
        final Logger logger
    ) {
        this.channelRegistry = channelRegistry;
        this.configManager = configManager;
        this.logger = logger;
        this.config = this.config(configManager, configMeta());
    }

    @Override
    public boolean eligible() {
        return this.config.enabled && Bukkit.getPluginManager().isPluginEnabled("Parties");
    }

    @Override
    public void register() {
        if (this.config.partyChannel) {
            if (this.configManager.primaryConfig().partyChat().enabled) {
                this.logger.warn("Both CarbonChat parties and the Parties party chat channel are enabled!");
                this.logger.warn("Usually, you want one or the other enabled. Additionally, their default channel configs will conflict.");
            }
            this.channelRegistry.registerSpecialConfigChannel(PartiesPartyChannel.FILE_NAME, PartiesPartyChannel.class);
        }
    }

    public static ConfigMeta configMeta() {
        return Integration.configMeta("parties", PartiesIntegration.Config.class);
    }

    @ConfigSerializable
    public static final class Config {

        boolean enabled = true;

        @Comment("You will likely want to disable Carbon's built-in party system above when using Parties party chat.")
        boolean partyChannel = true;

    }

}
