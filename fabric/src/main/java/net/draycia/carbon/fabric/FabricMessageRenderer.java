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
import io.github.miniplaceholders.api.MiniPlaceholders;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.moonshine.message.IMessageRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class FabricMessageRenderer<T extends Audience> implements IMessageRenderer<T, String, Component, Component> {

    private final ConfigFactory configFactory;

    @Inject
    public FabricMessageRenderer(final ConfigFactory configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public Component render(
        final T receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final TagResolver.Builder tagResolver = TagResolver.builder();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            tagResolver.tag(entry.getKey(), Tag.inserting(entry.getValue()));
        }

        final String placeholderResolvedMessage = this.configFactory.primaryConfig().applyCustomPlaceholders(intermediateMessage);

        if (FabricLoader.getInstance().isModLoaded("miniplaceholders")) {
            tagResolver.resolver(MiniPlaceholders.getGlobalPlaceholders());

            if (receiver instanceof SourcedAudience sourced) {
                if (sourced.sender() instanceof CarbonPlayerFabric sender) {
                    tagResolver.resolver(MiniPlaceholders.getAudiencePlaceholders(sender));
                    if (sourced.recipient() instanceof CarbonPlayerFabric recipient && recipient.online()) {
                        tagResolver.resolver(MiniPlaceholders.getRelationalPlaceholders(sender, recipient));
                    }
                }
            }
        }

        return MiniMessage.miniMessage().deserialize(placeholderResolvedMessage, tagResolver.build());
    }

}
