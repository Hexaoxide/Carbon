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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.events.CarbonReloadEvent;
import net.draycia.carbon.common.events.ChannelRegisterEvent;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.registry.DefaultedRegistryGetter;
import ninja.egg82.messenger.services.PacketService;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.text.Component.empty;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonChannelRegistry implements ChannelRegistry, DefaultedRegistryGetter<Key, ChatChannel> {

    private static @MonotonicNonNull ObjectMapper<ConfigChatChannel> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.factory().get(ConfigChatChannel.class);
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
    }

    private final Path configChannelDir;
    private final Injector injector;
    private final Logger logger;
    private final ConfigFactory configFactory;
    private @MonotonicNonNull Key defaultKey;
    //private @MonotonicNonNull ChatChannel basicChannel;
    private final CarbonMessages carbonMessages;
    private final CarbonChat carbonChat;

    private final BiMap<Key, ChatChannel> channelMap = Maps.synchronizedBiMap(HashBiMap.create());

    @Inject
    public CarbonChannelRegistry(
        @ForCarbon final Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final ConfigFactory configFactory,
        final CarbonMessages carbonMessages,
        //final BasicChatChannel basicChannel,
        final CarbonChat carbonChat
    ) {
        this.configChannelDir = dataDirectory.resolve("channels");
        this.injector = injector;
        this.logger = logger;
        this.configFactory = configFactory;
        this.carbonMessages = carbonMessages;
        //this.basicChannel = basicChannel;
        this.carbonChat = carbonChat;

        carbonChat.eventHandler().subscribe(CarbonReloadEvent.class, event -> {
            this.reloadRegisteredConfigChannels();
        });
    }

    public static ConfigurationTransformation.Versioned versioned() {
        return ConfigurationTransformation.versionedBuilder()
            .addVersion(0, initialTransform())
            .build();
    }

    private static ConfigurationTransformation initialTransform() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path(), (path, value) -> {
                value.node("radius").set(-1);

                return null;
            })
            .build();
    }

    // https://github.com/SpongePowered/Configurate/blob/1ec74f6474237585aee858b636d9761d237839d5/examples/src/main/java/org/spongepowered/configurate/examples/Transformations.java#L107
    /**
     * Apply the transformations to a node.
     *
     * <p>This method also prints information about the version update that
     * occurred</p>
     *
     * @param node the node to transform
     * @param <N> node type
     * @return provided node, after transformation
     */
    public static <N extends ConfigurationNode> N updateNode(final N node) throws ConfigurateException {
        if (!node.virtual()) { // we only want to migrate existing data
            final ConfigurationTransformation.Versioned trans = versioned();
            final int startVersion = trans.version(node);
            trans.apply(node);
            final int endVersion = trans.version(node);

            if (startVersion != endVersion) { // we might not have made any changes
                // TODO: use logger
                CarbonChatProvider.carbonChat().logger().info("Updated config schema from " + startVersion + " to " + endVersion);
            }
        }

        return node;
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

    public void loadConfigChannels(final CarbonMessages carbonMessages) {
        this.defaultKey = this.configFactory.primaryConfig().defaultChannel();

        if (!Files.exists(this.configChannelDir)) {
            // no channels to register, register default channel
            this.registerDefaultChannel();
            return;
        } else if (this.isPathEmpty(this.configChannelDir)) {
            final ChatChannel basicChannel = this.injector.getInstance(BasicChatChannel.class);

            this.register(basicChannel.key(), basicChannel);
        }

        // otherwise, register all channels found
        try (final Stream<Path> paths = Files.walk(this.configChannelDir)) {
            paths.forEach(path -> {
                final String fileName = path.getFileName().toString();

                if (!fileName.endsWith(".conf")) {
                    return;
                }

                final @Nullable ChatChannel chatChannel = this.registerChannelFromPath(path);

                if (chatChannel == null) {
                    this.logger.warn("Failed to load channel from file [" + fileName + "]");
                    return;
                }

                if (chatChannel.shouldRegisterCommands()) {
                    this.registerChannelCommands(chatChannel);
                }
            });

            if (!this.channelMap.containsKey(this.defaultKey)) {
                this.logger.warn("No default channel found! Default channel key: [" + this.defaultKey().asString() + "]");
            }

            final List<String> channelList = new ArrayList<>();

            for (final ChatChannel chatChannel : this.channelMap.values()) {
                channelList.add(chatChannel.key().asString());
            }

            final String channels = String.join(", ", channelList);

            this.logger.warn("Registered channels: [" + channels + "]");
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        this.carbonChat.eventHandler().emit(new ChannelRegisterEvent(this.channelMap.values(), this));
    }

    public @Nullable ChatChannel loadChannel(final Path channelFile) {
        final ConfigurationLoader<?> loader = this.configFactory.configurationLoader(channelFile);

        try {
            final var loaded = updateNode(loader.load());
            loader.save(loaded);
            return MAPPER.load(loaded);
        } catch (final ConfigurateException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private void registerDefaultChannel() {
        try {
            Files.createDirectories(this.configChannelDir);

            final Path configFile = this.configChannelDir.resolve("global.conf");

            final var loader = this.configFactory.configurationLoader(configFile);
            final var node = loader.load();

            final var configChannel = this.injector.getInstance(ConfigChatChannel.class);

            node.set(ConfigChatChannel.class, configChannel);
            loader.save(node);

            this.register(configChannel.key(), configChannel);
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

        if (this.defaultKey.equals(channelKey)) {
            this.logger.info("Default channel is [" + channelKey + "]");
        }

        this.register(channelKey, channel);

        return channel;
    }

    private void sendMessageInChannelAsPlayer(
        final CarbonPlayer sender,
        final ChatChannel channel,
        final String plainMessage
    ) {
        // Should we silent exit here? Chances are whatever caused the
        if (!sender.speechPermitted(plainMessage)) {
            return;
        }

        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(Key.key("carbon", "default"), channel));

        // TODO: add previewing when cloud/adventure support it
        final var chatEvent = new CarbonChatEvent(sender, Component.text(plainMessage), recipients, renderers, channel, false);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

        for (final var renderer : chatEvent.renderers()) {
            renderedMessage = renderer.render(sender, sender, renderedMessage.component(), chatEvent.message());
        }

        final Identity identity = sender.hasPermission("carbon.hideidentity") ? Identity.nil() : sender.identity();

        for (final Audience recipient : chatEvent.recipients()) {
            recipient.sendMessage(identity, renderedMessage.component(), renderedMessage.messageType());
        }

        final @Nullable PacketService packetService = this.carbonChat.packetService();

        if (packetService != null) {
            if (channel instanceof ConfigChatChannel configChatChannel) {
                final @Nullable String format = configChatChannel.messageFormat(sender);

                packetService.queuePacket(new ChatMessagePacket(this.carbonChat.serverId(), sender.uuid(),
                    configChatChannel.permission(), channel.key(), sender.username(), format,
                    Map.of("username", sender.username(), "message", plainMessage)));
                packetService.flushQueue();
            }
        }
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
    public void registerChannelCommands(final ChatChannel channel) {
        final CommandManager<Commander> commandManager =
            this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<CommandManager<Commander>>() {}));

        var builder = commandManager.commandBuilder(channel.commandName(),
                channel.commandAliases(), commandManager.createDefaultCommandMeta())
            .argument(StringArgument.<Commander>newBuilder("message").greedy().asOptional().build());

        if (channel.permission() != null) {
            builder = builder.permission(channel.permission());

            // Add to LuckPerms permission suggestions... lol
            //this.carbonChat.server().console().get(PermissionChecker.POINTER).ifPresent(checker -> {
            //    checker.test(channel.permission());
            //    checker.test(channel.permission() + ".see");
            //    checker.test(channel.permission() + ".speak");
            //});
        }

        final Key channelKey = channel.key();

        final var command = builder.senderType(PlayerCommander.class)
            .handler(handler -> {
                final var sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final @Nullable ChatChannel chatChannel = this.get(channelKey);

                if (sender.muted()) {
                    this.carbonMessages.muteCannotSpeak(sender);
                    return;
                }

                if (handler.contains("message")) {
                    final String message = handler.get("message");

                    // TODO: trigger platform events related to chat
                    this.sendMessageInChannelAsPlayer(sender, chatChannel, message);
                } else {
                    sender.selectedChannel(chatChannel);
                    this.carbonMessages.changedChannels(sender, channelKey.value());
                }
            })
            .build();

        commandManager.command(command);

        final var channelCommand = commandManager.commandBuilder("channel", "ch")
            .literal(channelKey.value())
            .proxies(command)
            .build();

        commandManager.command(channelCommand);
    }

    @Override
    public @NonNull ChatChannel register(final @NonNull Key key, final @NonNull ChatChannel value) {
        this.channelMap.put(key, value);
        return value;
    }

    @Override
    public @Nullable ChatChannel get(final @NonNull Key key) {
        return this.channelMap.get(key);
    }

    @Override
    public @Nullable Key key(final @NonNull ChatChannel value) {
        return this.channelMap.inverse().get(value);
    }

    @Override
    public @NonNull Set<Key> keySet() {
        return Collections.unmodifiableSet(this.channelMap.keySet());
    }

    @Override
    public @NonNull Iterator<ChatChannel> iterator() {
        return Iterators.unmodifiableIterator(this.channelMap.values().iterator());
    }

    @Override
    public ChatChannel defaultValue() {
        return Objects.requireNonNull(this.get(this.defaultKey));
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
