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
package net.draycia.carbon.paper.integration.towny;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.channels.CarbonChannelRegistry.SpecialHandler;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.integration.Integration;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
public final class TownyIntegration implements Integration {

    private final Injector injector;
    private final CarbonChannelRegistry channelRegistry;
    private final Config config;

    @Inject
    public TownyIntegration(
        final Injector injector,
        final CarbonChannelRegistry channelRegistry,
        final ConfigManager configManager
    ) {
        this.injector = injector;
        this.channelRegistry = channelRegistry;
        this.config = this.config(configManager, configMeta());
    }

    @Override
    public boolean eligible() {
        return this.config.enabled && Bukkit.getPluginManager().isPluginEnabled("Towny");
    }

    @Override
    public void register() {
        if (this.config.townChannel) {
            this.channelRegistry.registerSpecialConfigChannel(
                TownChannel.FILE_NAME,
                new SpecialHandler<>(TownChannel.class, () -> this.injector.getInstance(TownChannel.class))
            );
        }

        if (this.config.nationChannel) {
            this.channelRegistry.registerSpecialConfigChannel(
                NationChannel.FILE_NAME,
                new SpecialHandler<>(NationChannel.class, () -> this.injector.getInstance(NationChannel.class))
            );
        }

        if (this.config.allianceChannel) {
            this.channelRegistry.registerSpecialConfigChannel(
                AllianceChannel.FILE_NAME,
                new SpecialHandler<>(AllianceChannel.class, () -> this.injector.getInstance(AllianceChannel.class))
            );
        }
    }

    public static ConfigMeta configMeta() {
        return Integration.configMeta("towny", TownyIntegration.Config.class);
    }

    @ConfigSerializable
    public static final class Config {
        boolean enabled = true;
        boolean townChannel = true;
        boolean nationChannel = true;
        boolean allianceChannel = true;
    }

}
