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
package net.draycia.carbon.paper.messages;

import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import io.github.miniplaceholders.api.MiniPlaceholders;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.users.ConsoleCarbonPlayer;
import net.draycia.carbon.paper.CarbonChatPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.moonshine.message.IMessageRenderer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public class PaperMessageRenderer<T extends Audience> implements IMessageRenderer<T, String, Component, Component> {

    private final Supplier<@MonotonicNonNull PlaceholderAPIMiniMessageParser> placeholderApiProcessor = Suppliers.memoize(() -> {
        if (CarbonChatPaper.papiLoaded()) {
            return PlaceholderAPIMiniMessageParser.create(MiniMessage.miniMessage());
        }
        return null;
    });

    private final MiniMessage miniMessage;
    private final ConfigFactory configFactory;

    @Inject
    public PaperMessageRenderer(final ConfigFactory configFactory) {
        this.miniMessage = MiniMessage.miniMessage();
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

        if (CarbonChatPaper.miniPlaceholdersLoaded()) {
            tagResolver.resolver(MiniPlaceholders.getGlobalPlaceholders());
        }

        if (!(receiver instanceof SourcedAudience sourced)) {
            return this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
        }

        if (!(sourced.sender() instanceof CarbonPlayer sender && sender.online())) {
            return this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
        }

        // We can't/shouldn't resolve placeholders for non-players
        if (sender instanceof ConsoleCarbonPlayer) {
            return this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
        }

        final Player senderBukkitPlayer = requireNonNull(Bukkit.getPlayer(sender.uuid()));

        if (CarbonChatPaper.miniPlaceholdersLoaded()) {
            tagResolver.resolver(MiniPlaceholders.getAudiencePlaceholders(senderBukkitPlayer));
        }

        if (!(sourced.recipient() instanceof CarbonPlayer recipient && recipient.online())) {
            if (this.hasPlaceholderAPI()) {
                return this.placeholderApiProcessor.get().parse(senderBukkitPlayer,
                    placeholderResolvedMessage, tagResolver.build());
            }
            return this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
        }

        final @Nullable Player recipientBukkitPlayer = Bukkit.getPlayer(recipient.uuid());
        if (recipientBukkitPlayer == null) {
            if (this.hasPlaceholderAPI()) {
                return this.placeholderApiProcessor.get().parse(senderBukkitPlayer,
                    placeholderResolvedMessage, tagResolver.build());
            }
            return this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
        }

        if (CarbonChatPaper.miniPlaceholdersLoaded()) {
            tagResolver.resolver(MiniPlaceholders.getRelationalPlaceholders(
                senderBukkitPlayer,
                recipientBukkitPlayer
            ));
        }
        if (this.hasPlaceholderAPI()) {
            return this.placeholderApiProcessor.get().parseRelational(senderBukkitPlayer,
                recipientBukkitPlayer, placeholderResolvedMessage, tagResolver.build());
        }

        return this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
    }

    private boolean hasPlaceholderAPI() {
        return this.placeholderApiProcessor.get() != null;
    }

}
