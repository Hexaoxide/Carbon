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

@FunctionalInterface
public interface RecipientsResolver {
    List<Audience> recipients(CarbonPlayer sender);

    default RecipientsResolver and(final RecipientsResolver resolver) {
        return sender -> {
            final List<Audience> recipients = new ArrayList<>(this.recipients(sender));
            recipients.addAll(resolver.recipients(sender));
            return recipients;
        };
    }

    static Builder builder(final CarbonServer server) {
        return new Builder(server);
    }

    final class Builder {
        private final CarbonServer server;
        private final List<Predicate<CarbonPlayer>> playerFilters = new ArrayList<>();
        private boolean includeConsole;

        private Builder(final CarbonServer server) {
            this.server = server;
        }

        public @This Builder permittedPlayersInChannel(final ChatChannel channel) {
            return this.permittedPlayersInChannel(channel.permissions(), channel.key());
        }

        public @This Builder permittedPlayersInChannel(final ChannelPermissions permissions, final Key channelKey) {
            this.playerFilters.add(player -> permissions.hearingPermitted(player).permitted()
                && !player.leftChannels().contains(channelKey));
            return this;
        }

        public @This Builder players(final Predicate<CarbonPlayer> filter) {
            this.playerFilters.add(filter);
            return this;
        }

        public @This Builder console() {
            this.includeConsole = true;
            return this;
        }

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
