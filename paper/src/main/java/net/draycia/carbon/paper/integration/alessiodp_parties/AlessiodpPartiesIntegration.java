/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.paper.integration.alessiodp_parties;

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
public class AlessiodpPartiesIntegration implements Integration {

    private final CarbonChannelRegistry channelRegistry;
    private final ConfigManager configManager;
    private final Logger logger;
    private final AlessiodpPartiesIntegration.Config config;

    @Inject
    public AlessiodpPartiesIntegration(
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
        try {
            Class.forName("com.alessiodp.parties.api.Parties");
            return this.config.enabled && Bukkit.getPluginManager().isPluginEnabled("Parties");
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public void register() {
        if (this.config.partyChannel) {
            if (this.configManager.primaryConfig().partyChat().enabled) {
                this.logger.warn("Both CarbonChat parties and the Parties party chat channel are enabled!");
                this.logger.warn("Usually, you want one or the other enabled. Additionally, their default channel configs will conflict.");
            }
            this.channelRegistry.registerSpecialConfigChannel(AlessiodpPartiesPartyChannel.FILE_NAME, AlessiodpPartiesPartyChannel.class);
        }
    }

    public static ConfigMeta configMeta() {
        return Integration.configMeta("alessiodp-parties", AlessiodpPartiesIntegration.Config.class);
    }

    @ConfigSerializable
    public static final class Config {

        boolean enabled = true;

        @Comment("You will likely want to disable Carbon's built-in party system above when using Parties party chat.")
        boolean partyChannel = true;

    }

}
