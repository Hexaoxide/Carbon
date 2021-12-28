/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@DefaultQualifier(Nullable.class)
@ConfigSerializable
public class MessagingSettings {

    @Comment("Options: RABBITMQ, NATS, REDIS")
    private MessagingManager.@NonNull BrokerType brokerType = MessagingManager.BrokerType.NONE;

    @Comment("")
    private final String url = "127.0.0.1";

    @Comment("")
    private final int port = 5672; // RabbitMQ 5672, NATS 4222, Redis 6379

    @Comment("RabbitMQ VHost")
    private final @Nullable String vhost = "/"; // RabbitMQ only

    @Comment("NATS credentials file")
    private final @Nullable String credentialsFile = ""; // NATS only

    @Comment("RabbitMQ username")
    private final @Nullable String username = "username"; // RabbitMQ only

    @Comment("RabbitMQ and Redis password")
    private final @Nullable String password = "password"; // RabbitMQ and Redis only

    public MessagingManager.@NonNull BrokerType brokerType() {
        return this.brokerType;
    }

    public String url() {
        return this.url;
    }

    public int port() {
        return this.port;
    }

    public String vhost() {
        return this.vhost;
    }

    public String credentialsFile() {
        return this.credentialsFile;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

}
