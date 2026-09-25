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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.plugin.PluginManager;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.integration.miniplaceholders.MiniPlaceholdersIntegration;
import net.draycia.carbon.common.integration.miniplaceholders.MiniPlaceholdersUtil;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.RenderForTagResolver;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public class VelocityMessageRenderer extends CarbonMessageRenderer {

    private final ConfigManager configManager;
    private final PluginManager pluginManager;

    @Inject
    public VelocityMessageRenderer(final ConfigManager configManager, final PluginManager pluginManager, final RenderForTagResolver.Factory renderForTagResolver) {
        super(renderForTagResolver);
        this.configManager = configManager;
        this.pluginManager = pluginManager;
    }

    @Override
    public Component render(
        final Audience receiver,
        final String intermediateMessage,
        final TagResolver.Builder tagResolver
    ) {
        final String placeholderResolvedMessage = this.configManager.primaryConfig().applyCustomPlaceholders(intermediateMessage);

        final MiniPlaceholdersIntegration.@Nullable Config miniplaceholdersConfig = MiniPlaceholdersUtil.miniPlaceholdersLoaded()
            ? this.configManager.primaryConfig().integrations().config(MiniPlaceholdersIntegration.configMeta())
            : null;

        if (miniplaceholdersConfig != null) {
            tagResolver.resolver(MiniPlaceholders.globalPlaceholders());

            if (receiver instanceof SourcedAudience) {
                tagResolver.resolver(MiniPlaceholders.audiencePlaceholders());
                if (miniplaceholdersConfig.relationalPlaceholders) {
                    tagResolver.resolver(MiniPlaceholders.relationalPlaceholders());
                }
            }
        }

        final Audience parseAudience = receiver instanceof SourcedAudience sourced
            ? MiniPlaceholdersUtil.wrapAudiences(miniplaceholdersConfig, sourced.recipient(), sourced.sender())
            : receiver;

        return MiniMessage.miniMessage().deserialize(placeholderResolvedMessage, parseAudience, tagResolver.build());
    }

}
