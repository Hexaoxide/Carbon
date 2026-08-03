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
package net.draycia.carbon.common.config;

import net.draycia.carbon.common.messaging.MessagingManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@NullMarked
@ConfigSerializable
public class MessagingSettings {

    @Comment("Whether cross-server messaging is enabled")
    private boolean enabled = false;

    @Comment("One of: RABBITMQ, NATS, REDIS")
    private MessagingManager.@NonNull BrokerType brokerType = MessagingManager.BrokerType.NONE;

    private @MonotonicNonNull @Nullable String url = "127.0.0.1";

    private int port = 5672; // RabbitMQ 5672, NATS 4222, Redis 6379

    @Comment("RabbitMQ VHost")
    private @MonotonicNonNull @Nullable String vhost = "/"; // RabbitMQ only

    @Comment("NATS credentials file")
    private @MonotonicNonNull @Nullable String credentialsFile = ""; // NATS only

    @Comment("RabbitMQ username")
    private @MonotonicNonNull @Nullable String username = "username"; // RabbitMQ only

    @Comment("RabbitMQ and Redis password")
    private @MonotonicNonNull @Nullable String password = "password"; // RabbitMQ and Redis only

    public boolean enabled() {
        return this.enabled;
    }

    public MessagingManager.@NonNull BrokerType brokerType() {
        return this.brokerType;
    }

    public @MonotonicNonNull @Nullable String url() {
        return this.url;
    }

    public int port() {
        return this.port;
    }

    public @MonotonicNonNull @Nullable String vhost() {
        return this.vhost;
    }

    public @MonotonicNonNull @Nullable String credentialsFile() {
        return this.credentialsFile;
    }

    public @MonotonicNonNull @Nullable String username() {
        return this.username;
    }

    public @MonotonicNonNull @Nullable String password() {
        return this.password;
    }

}
