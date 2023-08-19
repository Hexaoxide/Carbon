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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.plugin.PluginManager;
import io.github.miniplaceholders.api.MiniPlaceholders;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.users.ConsoleCarbonPlayer;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public class VelocityMessageRenderer implements CarbonMessageRenderer {

    private final ConfigManager configManager;
    private final PluginManager pluginManager;

    @Inject
    public VelocityMessageRenderer(final ConfigManager configManager, final PluginManager pluginManager) {
        this.configManager = configManager;
        this.pluginManager = pluginManager;
    }

    @Override
    public Component render(
        final Audience receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final TagResolver.Builder tagResolver = TagResolver.builder();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            tagResolver.tag(entry.getKey(), Tag.inserting(entry.getValue()));
        }

        final String placeholderResolvedMessage = this.configManager.primaryConfig().applyCustomPlaceholders(intermediateMessage);

        if (this.pluginManager.isLoaded("miniplaceholders")) {
            tagResolver.resolver(MiniPlaceholders.getGlobalPlaceholders());

            if (receiver instanceof SourcedAudience sourced) {
                if (sourced.sender() instanceof CarbonPlayerVelocity sender) {
                    tagResolver.resolver(MiniPlaceholders.getAudiencePlaceholders(sender));
                    if (sourced.recipient() instanceof CarbonPlayerVelocity recipient) {
                        tagResolver.resolver(MiniPlaceholders.getRelationalGlobalPlaceholders(sender, recipient));
                    }
                } else if (sourced.sender() instanceof ConsoleCarbonPlayer console) {
                    // I don't know if this will ever actually resolve anything, or if anything supports console audience
                    tagResolver.resolver(MiniPlaceholders.getAudiencePlaceholders(console));
                }
            }
        }

        return MiniMessage.miniMessage().deserialize(placeholderResolvedMessage, tagResolver.build());
    }

}
