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
package net.draycia.carbon.fabric;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.miniplaceholders.api.MiniPlaceholders;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.users.ConsoleCarbonPlayer;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public class FabricMessageRenderer implements CarbonMessageRenderer {

    private final ConfigManager configManager;

    @Inject
    public FabricMessageRenderer(final ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Component render(
        final Audience receiver,
        final String intermediateMessage,
        final Map<String, ?> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final TagResolver.Builder tagResolver = TagResolver.builder();

        CarbonMessageRenderer.addResolved(tagResolver, resolvedPlaceholders);

        final String placeholderResolvedMessage = this.configManager.primaryConfig().applyCustomPlaceholders(intermediateMessage);

        if (FabricLoader.getInstance().isModLoaded("miniplaceholders")) {
            tagResolver.resolver(MiniPlaceholders.getGlobalPlaceholders());

            if (receiver instanceof SourcedAudience sourced) {
                if (sourced.sender() instanceof CarbonPlayerFabric sender) {
                    tagResolver.resolver(MiniPlaceholders.getAudiencePlaceholders(sender));
                    if (sourced.recipient() instanceof CarbonPlayerFabric recipient && recipient.online()) {
                        tagResolver.resolver(MiniPlaceholders.getRelationalPlaceholders(sender, recipient));
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
