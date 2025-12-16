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
package net.draycia.carbon.common.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.integration.Integration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@DefaultQualifier(NonNull.class)
public final class MiniPlaceholdersIntegration implements Integration {

    private final Provider<MiniPlaceholdersExpansion> expansionProvider;
    private final Config config;

    @Inject
    private MiniPlaceholdersIntegration(
        final ConfigManager configManager,
        final Provider<MiniPlaceholdersExpansion> expansionProvider
    ) {
        this.expansionProvider = expansionProvider;
        this.config = this.config(configManager, configMeta());
    }

    @Override
    public boolean eligible() {
        return MiniPlaceholdersUtil.miniPlaceholdersLoaded();
    }

    @Override
    public void register() {
        this.expansionProvider.get().registerExpansion();
    }

    public static ConfigMeta configMeta() {
        return Integration.configMeta("miniplaceholders", MiniPlaceholdersIntegration.Config.class);
    }

    @ConfigSerializable
    public static final class Config {
        @Comment("Enabling relational placeholders may require reworking your format configs due to the way MiniPlaceholders v3 works.")
        public boolean relationalPlaceholders = false;
    }

}
