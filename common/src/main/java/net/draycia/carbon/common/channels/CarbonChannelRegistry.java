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
package net.draycia.carbon.common.channels;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.registry.DefaultedRegistry;
import net.kyori.registry.RegistryImpl;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonChannelRegistry extends RegistryImpl<Key, ChatChannel> implements ChannelRegistry, DefaultedRegistry<Key, ChatChannel> {

    private static @MonotonicNonNull ObjectMapper<ConfigChatChannel> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.factory().get(ConfigChatChannel.class);
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
    }

    private final ConfigFactory configLoader;
    private final Path dataDirectory;
    private final Injector injector;
    private final Logger logger;
    private final ConfigFactory configFactory;
    private @MonotonicNonNull Key defaultKey;
    private @MonotonicNonNull ChatChannel basicChannel;
    private final CarbonMessageService messageService;

    @Inject
    public CarbonChannelRegistry(
        final ConfigFactory configLoader,
        @ForCarbon final Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final ConfigFactory configFactory,
        final CarbonMessageService messageService
    ) {
        this.configLoader = configLoader;
        this.dataDirectory = dataDirectory;
        this.injector = injector;
        this.logger = logger;
        this.configFactory = configFactory;
        this.messageService = messageService;
    }

    public void loadChannels() {
        final Path channelDirectory = this.dataDirectory.resolve("channels");
        this.basicChannel = this.injector.getInstance(BasicChatChannel.class);
        this.defaultKey = this.configFactory.primaryConfig().defaultChannel();

        if (!Files.exists(channelDirectory)) {
            // folder doesn't exist, no channels setup
            try {
                Files.createDirectories(channelDirectory);

                this.register(this.basicChannel.key(), this.basicChannel);

                final Path configFile = channelDirectory.resolve("basic-channel.conf.example");

                final var loader = this.configLoader.configurationLoader(configFile);
                final var node = loader.load();

                final var configChannel = this.injector.getInstance(ConfigChatChannel.class);
                node.set(ConfigChatChannel.class, configChannel);

                loader.save(node);

                // TODO: create advanced-channel.conf.example
                // TODO: log in console, "no channels found - adding example configs"
            } catch (final IOException exception) {
                exception.printStackTrace();
            }

            return;
        }

        try (final Stream<Path> paths = Files.walk(channelDirectory)) {
            final CommandManager<Commander> commandManager = this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<CommandManager<Commander>>() {}));

            paths.forEach(path -> {
                final String fileName = path.getFileName().toString();

                if (fileName.endsWith(".conf")) {
                    final @Nullable ChatChannel channel = this.loadChannel(path);

                    if (channel != null) {
                        final Key channelKey = channel.key();

                        this.logger.info("Registering channel with key [" + channelKey + "]");
                        register(channelKey, channel);

                        final var command = commandManager.commandBuilder(channelKey.value())
                            .argument(StringArgument.<Commander>newBuilder("message").greedy().asOptional().build())
                            .permission("carbon.channel." + channelKey)
                            .senderType(PlayerCommander.class)
                            .handler(handler -> {
                                final var sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                                if (sender.muted(channel)) {
                                    // TODO: "you are muted in this channel!!!!"
                                    return;
                                }

                                if (handler.contains("message")) {
                                    final String message = handler.get("message");
                                    final var component = Component.text(message);

                                    // TODO: trigger platform events related to chat
                                    // TODO: also make sure carbon events are also emitted properly?
                                    for (final var recipient : channel.recipients(sender)) {
                                        final var renderedMessage = channel.render(sender, recipient, component, component);
                                        recipient.sendMessage(renderedMessage.component(), renderedMessage.messageType());
                                    }
                                } else {
                                    sender.selectedChannel(channel);
                                    this.messageService.changedChannels(sender, channel.key().value());
                                }
                            })
                            .build(); // TODO: command aliases

                        commandManager.command(command);

                        final var channelCommand = commandManager.commandBuilder("channel", "ch")
                            .literal(channelKey.value())
                            .proxies(command)
                            .build();

                        commandManager.command(channelCommand);
                    }
                }
            });
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public @Nullable ChatChannel loadChannel(final Path channelFile) {
        final ConfigurationLoader<?> loader = this.configLoader.configurationLoader(channelFile);

        try {
            return MAPPER.load(loader.load());
        } catch (final ConfigurateException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public ChatChannel defaultValue() {
        return Objects.requireNonNullElse(this.get(this.defaultKey), this.basicChannel);
    }

    @Override
    public Key defaultKey() {
        return this.defaultKey;
    }

    @Override
    public ChatChannel getOrDefault(final Key key) {
        final @Nullable ChatChannel channel = this.get(key);

        if (channel != null) {
            return channel;
        }

        return this.defaultValue();
    }

}
