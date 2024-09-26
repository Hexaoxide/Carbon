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
package net.draycia.carbon.api.channels;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Resolves the recipients for a message from a given sender.
 *
 * @see ChatChannel#recipientsResolver()
 * @see #builder(CarbonServer)
 * @since 3.0.0
 */
@FunctionalInterface
public interface RecipientsResolver {

    /**
     * Resolves the recipients for a message from a given sender.
     *
     * @param sender the sender to resolve recipients for
     * @return resolved recipients
     * @since 3.0.0
     */
    List<Audience> recipients(CarbonPlayer sender);

    /**
     * Creates a new combined {@link RecipientsResolver} from this resolver and {@code resolver}.
     *
     * <p>
     * Note that this method does not filter duplicate audiences.
     * </p>
     *
     * @param resolver second resolver
     * @return new combined resolver
     * @since 3.0.0
     */
    default RecipientsResolver and(final RecipientsResolver resolver) {
        return sender -> {
            final List<Audience> recipients = new ArrayList<>(this.recipients(sender));
            recipients.addAll(resolver.recipients(sender));
            return recipients;
        };
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @param server server instance
     * @return new {@link Builder}
     * @since 3.0.0
     */
    static Builder builder(final CarbonServer server) {
        return new Builder(server);
    }

    /**
     * Mutable builder for {@link RecipientsResolver}.
     * <p>
     * Custom implementations are supported, however the builder simplifies most use cases.
     * </p>
     *
     * @since 3.0.0
     */
    final class Builder {
        private final CarbonServer server;
        private final List<Predicate<CarbonPlayer>> playerFilters = new ArrayList<>();
        private boolean includeConsole;

        private Builder(final CarbonServer server) {
            this.server = server;
        }

        /**
         * Includes online players with permission to hear the channel that have not left the channel,
         * using {@link #players(Predicate)}.
         *
         * @param channel channel
         * @return this builder
         * @since 3.0.0
         */
        public @This Builder permittedPlayersInChannel(final ChatChannel channel) {
            return this.permittedPlayersInChannel(channel.permissions(), channel.key());
        }

        /**
         * Includes online players with permission to hear the channel that have not left the channel,
         * using {@link #players(Predicate)}.
         *
         * @param permissions permissions
         * @param channelKey channel
         * @return this builder
         * @since 3.0.0
         */
        public @This Builder permittedPlayersInChannel(final ChannelPermissions permissions, final Key channelKey) {
            return this.players(player -> permissions.hearingPermitted(player).permitted()
                && !player.leftChannels().contains(channelKey));
        }

        /**
         * Includes online players matching the predicate in the resolved recipients. Adding multiple predicates
         * will include players matching any one of the predicate.
         *
         * @param filter player filter
         * @return this builder
         * @since 3.0.0
         */
        public @This Builder players(final Predicate<CarbonPlayer> filter) {
            this.playerFilters.add(filter);
            return this;
        }

        /**
         * Unconditionally includes the console in the resolved recipients.
         *
         * @return this builder
         * @since 3.0.0
         */
        public @This Builder console() {
            this.includeConsole = true;
            return this;
        }

        /**
         * Builds a new {@link RecipientsResolver} from the current state of this builder.
         *
         * @return new {@link RecipientsResolver} instance
         * @since 3.0.0
         */
        public RecipientsResolver build() {
            final boolean includeConsole = this.includeConsole;
            final List<Predicate<CarbonPlayer>> playerFilters = List.copyOf(this.playerFilters);

            return sender -> {
                final List<Audience> recipients = new ArrayList<>();

                if (!playerFilters.isEmpty()) {
                    for (final CarbonPlayer player : this.server.players()) {
                        if (playerFilters.stream().anyMatch(filter -> filter.test(player))) {
                            recipients.add(player);
                        }
                    }
                }

                if (includeConsole) {
                    recipients.add(this.server.console());
                }

                return recipients;
            };
        }
    }
}
