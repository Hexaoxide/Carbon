//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
// Modified to better fit in Carbon
//

package net.draycia.carbon.common.messages;

import com.google.common.collect.Streams;
import com.proximyst.moonshine.component.receiver.IReceiver;
import com.proximyst.moonshine.component.receiver.IReceiverResolver;
import com.proximyst.moonshine.component.receiver.ReceiverContext;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.draycia.carbon.api.CarbonServer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ServerReceiverResolver implements IReceiverResolver<Audience> {

    private final @NonNull CarbonServer carbonServer;

    @Inject
    public ServerReceiverResolver(final @NonNull CarbonServer carbonServer) {
        this.carbonServer = carbonServer;
    }

    @Override
    public Optional<IReceiver<Audience>> resolve(final Method method) {
        final ServerReceiver annotation = method.getAnnotation(ServerReceiver.class);
        if (annotation == null) {
            return Optional.empty();
        }

        return Optional.of(new Resolver(this.carbonServer, annotation.permission()));
    }

    private static final class Resolver implements IReceiver<Audience> {
        private final @NonNull CarbonServer carbonServer;
        private final @Nullable String permission;

        private Resolver(final @NonNull CarbonServer carbonServer,
                         final @Nullable String permission) {
            this.carbonServer = carbonServer;
            this.permission = permission;
        }

        @Override
        public Audience find(final ReceiverContext ctx) {
            if (this.permission == null) {
                return this.carbonServer;
            }

            return Audience.audience(Stream.concat(Stream.of(this.carbonServer.console()),
                Streams.stream(this.carbonServer.players())
                    .filter(player -> player.hasPermission(this.permission)))
                    .collect(Collectors.toList()));
        }
    }

}
