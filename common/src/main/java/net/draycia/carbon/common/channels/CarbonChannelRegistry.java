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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
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
public class CarbonChannelRegistry implements ChannelRegistry, DefaultedRegistry<Key, ChatChannel> {

    private static @MonotonicNonNull ObjectMapper<ConfigChatChannel> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.factory().get(ConfigChatChannel.class);
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
    }

    private final ConfigFactory configLoader;
    private final Path configChannelDir;
    private final Injector injector;
    private final Logger logger;
    private final ConfigFactory configFactory;
    private @MonotonicNonNull Key defaultKey;
    private @MonotonicNonNull ChatChannel basicChannel;
    private final CarbonMessageService messageService;

    private final BiMap<Key, ChatChannel> map = Maps.synchronizedBiMap(HashBiMap.create());

    @Inject
    public CarbonChannelRegistry(
        final ConfigFactory configLoader,
        @ForCarbon final Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final ConfigFactory configFactory,
        final CarbonMessageService messageService,
        final BasicChatChannel basicChannel
    ) {
        this.configLoader = configLoader;
        this.configChannelDir = dataDirectory.resolve("channels");
        this.injector = injector;
        this.logger = logger;
        this.configFactory = configFactory;
        this.messageService = messageService;
        this.basicChannel = basicChannel;
    }

    public void reloadRegisteredConfigChannels() {
        try (final Stream<Path> paths = Files.walk(this.configChannelDir)) {
            paths.forEach(path -> {
                final String fileName = path.getFileName().toString();

                if (!fileName.endsWith(".conf")) {
                    return;
                }

                final @Nullable ChatChannel chatChannel = this.registerChannelFromPath(path);

                if (chatChannel == null) {
                    return;
                }

                final @Nullable ChatChannel existingChannel = this.get(chatChannel.key());

                if (existingChannel == null) {
                    return;
                }

                this.register(chatChannel.key(), chatChannel);
            });
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public void loadConfigChannels() {
        this.defaultKey = this.configFactory.primaryConfig().defaultChannel();

        if (!Files.exists(this.configChannelDir) || this.isPathEmpty(this.configChannelDir)) {
            // no channels to register, register default channel
            this.registerDefaultChannel();
            return;
        }

        // otherwise, register all channels found
        try (final Stream<Path> paths = Files.walk(this.configChannelDir)) {
            final CommandManager<Commander> commandManager =
                this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<CommandManager<Commander>>() {}));

            paths.forEach(path -> {
                final String fileName = path.getFileName().toString();

                if (!fileName.endsWith(".conf")) {
                    return;
                }

                final @Nullable ChatChannel chatChannel = this.registerChannelFromPath(path);

                if (chatChannel != null && chatChannel.shouldRegisterCommands()) {
                    this.registerChannelCommands(chatChannel, commandManager);
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

    private void registerDefaultChannel() {
        try {
            Files.createDirectories(this.configChannelDir);

            this.register(this.basicChannel.key(), this.basicChannel);

            final Path configFile = this.configChannelDir.resolve("basic-channel.conf.example");

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
    }

    private @Nullable ChatChannel registerChannelFromPath(final Path channelPath) {
        final @Nullable ChatChannel channel = this.loadChannel(channelPath);

        if (channel == null) {
            return null;
        }

        final Key channelKey = channel.key();

        this.logger.info("Registering channel with key [" + channelKey + "]");
        this.register(channelKey, channel);

        return channel;
    }

    private void registerChannelCommands(final ChatChannel channel, final CommandManager<Commander> commandManager) {
        final var command = commandManager.commandBuilder(channel.commandName(),
                channel.commandAliases(), commandManager.createDefaultCommandMeta())
            .argument(StringArgument.<Commander>newBuilder("message").greedy().asOptional().build())
            .permission("carbon.channel." + channel.key().value())
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
            .literal(channel.key().value())
            .proxies(command)
            .build();

        commandManager.command(channelCommand);
    }

    private boolean isPathEmpty(final Path path) {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
            return !directory.iterator().hasNext();
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public @NonNull ChatChannel register(final @NonNull Key key, final @NonNull ChatChannel value) {
        this.map.put(key, value);
        return value;
    }

    @Override
    public @Nullable ChatChannel get(final @NonNull Key key) {
        return this.map.get(key);
    }

    @Override
    public @Nullable Key key(final @NonNull ChatChannel value) {
        return this.map.inverse().get(value);
    }

    @Override
    public @NonNull Set<Key> keySet() {
        return Collections.unmodifiableSet(this.map.keySet());
    }

    @Override
    public @NonNull Iterator<ChatChannel> iterator() {
        return Iterators.unmodifiableIterator(this.map.values().iterator());
    }

}
