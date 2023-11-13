/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
package net.draycia.carbon.paper.integration.fuuid;

import com.google.inject.Inject;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.integration.Integration;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
public final class FactionsIntegration implements Integration {

    private final CarbonChannelRegistry channelRegistry;
    private final Config config;

    @Inject
    public FactionsIntegration(
        final CarbonChannelRegistry channelRegistry,
        final ConfigManager configManager
    ) {
        this.channelRegistry = channelRegistry;
        this.config = this.config(configManager, configMeta());
    }

    @Override
    public boolean eligible() {
        return this.config.enabled && Bukkit.getPluginManager().isPluginEnabled("Factions");
    }

    @Override
    public void register() {
        if (this.config.factionChannel) {
            this.channelRegistry.registerSpecialConfigChannel(FactionChannel.FILE_NAME, FactionChannel.class);
        }
        if (this.config.allianceChannel) {
            this.channelRegistry.registerSpecialConfigChannel(AllianceChannel.FILE_NAME, AllianceChannel.class);
        }
        if (this.config.truceChannel) {
            this.channelRegistry.registerSpecialConfigChannel(TruceChannel.FILE_NAME, TruceChannel.class);
        }
    }

    public static ConfigMeta configMeta() {
        return Integration.configMeta("factions", FactionsIntegration.Config.class);
    }

    @ConfigSerializable
    public static final class Config {

        boolean enabled = true;

        boolean factionChannel = true;
        boolean allianceChannel = true;
        boolean truceChannel = true;

    }

}
