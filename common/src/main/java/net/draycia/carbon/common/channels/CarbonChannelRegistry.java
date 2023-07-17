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
package net.draycia.carbon.common.channels;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.seiama.event.EventConfig;
import com.seiama.registry.Registry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChannelRegisterEvent;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.event.events.CarbonReloadEvent;
import net.draycia.carbon.common.event.events.ChannelRegisterEventImpl;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.Exceptions;
import net.draycia.carbon.common.util.FileUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonChannelRegistry extends ChatListenerInternal implements ChannelRegistry {

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
    private final CarbonMessages carbonMessages;
    private final CarbonEventHandler eventHandler;

    private volatile Registry<Key, ChatChannel> channelRegistry = Registry.create();
    private final Set<Key> configChannels = ConcurrentHashMap.newKeySet();
    //
    // private final BiMap<Key, ChatChannel> channelMap = Maps.synchronizedBiMap(HashBiMap.create());

    @Inject
    public CarbonChannelRegistry(
        @DataDirectory final Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final ConfigFactory configFactory,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler events
    ) {
        super(events, carbonMessages, configFactory);
        this.configChannelDir = dataDirectory.resolve("channels");
        this.injector = injector;
        this.logger = logger;
        this.configFactory = configFactory;
        this.carbonMessages = carbonMessages;
        this.eventHandler = events;

        events.subscribe(CarbonReloadEvent.class, -99, EventConfig.DEFAULT_ACCEPTS_CANCELLED, event -> this.reloadConfigChannels());
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
     * @param <N>  node type
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
                //CarbonChatProvider.carbonChat().logger().info("Updated config schema from " + startVersion + " to " + endVersion);
            }
        }

        return node;
    }

    public void reloadConfigChannels() {
        final Registry<Key, ChatChannel> newRegistry = Registry.create();

        // Copy API registrations over
        for (final Key registered : this.channelRegistry.keys()) {
            if (!this.configChannels.contains(registered)) {
                newRegistry.register(registered, this.channelRegistry.getHolder(registered).valueOrThrow());
            }
        }

        final Set<Key> oldConfigChannels = Set.copyOf(this.configChannels);
        this.configChannels.clear();

        final Registry<Key, ChatChannel> oldRegistry = this.channelRegistry;
        this.channelRegistry = newRegistry;

        this.loadConfigChannels_(this.carbonMessages);

        // Re-add any deleted channels; users must restart for them to be removed
        // (don't want to leave behind commands that just error, or confuse addons)
        for (final Key old : oldConfigChannels) {
            if (!this.configChannels.contains(old)) {
                this.configChannels.add(old);
                this.channelRegistry.register(old, oldRegistry.getHolder(old).valueOrThrow());
                this.logger.warn("The config file for channel [{}] was deleted, but removing " +
                    "channels at runtime is not currently supported. You must restart the plugin " +
                    "for the removal to take effect.", old);
            }
        }

        // Determine new channels and fire event if needed
        final Set<Key> newConfigChannels = new HashSet<>();
        for (final Key configChannel : this.configChannels) {
            if (!oldConfigChannels.contains(configChannel)) {
                newConfigChannels.add(configChannel);
            }
        }
        if (!newConfigChannels.isEmpty()) {
            this.eventHandler.emit(new ChannelRegisterEventImpl(this, Set.copyOf(newConfigChannels)));
        }
    }

    public void loadConfigChannels(final CarbonMessages messages) {
        this.loadConfigChannels_(messages);
        this.eventHandler.emit(new ChannelRegisterEventImpl(this, Set.copyOf(this.configChannels)));
    }

    private void loadConfigChannels_(final CarbonMessages messages) {
        this.defaultKey = this.configFactory.primaryConfig().defaultChannel();

        List<Path> channelConfigs = FileUtil.listDirectoryEntries(this.configChannelDir, "*.conf");
        if (channelConfigs.isEmpty()) {
            this.saveDefaultChannelConfig();
            channelConfigs = FileUtil.listDirectoryEntries(this.configChannelDir, "*.conf");
        }

        for (final Path channelConfigFile : channelConfigs) {
            final String fileName = channelConfigFile.getFileName().toString();
            if (!fileName.endsWith(".conf")) {
                continue;
            }

            final @Nullable ChatChannel chatChannel = this.loadChannel(channelConfigFile);
            if (chatChannel == null) {
                continue;
            }
            final Key channelKey = chatChannel.key();
            if (this.defaultKey.equals(channelKey)) {
                this.logger.info("Default channel is [" + channelKey + "]");
            }

            this.injector.injectMembers(chatChannel);
            this.configChannels.add(chatChannel.key());
            this.register(chatChannel, false);
        }

        if (this.channel(this.defaultKey) == null) {
            this.logger.warn("No default channel found! Default channel key: [" + this.defaultKey().asString() + "]");
        }

        final List<String> channelList = new ArrayList<>();

        for (final Key key : this.keys()) {
            channelList.add(key.asString());
        }

        final String channels = String.join(", ", channelList);

        this.logger.info("Registered channels: [" + channels + "]");
    }

    private void saveDefaultChannelConfig() {
        try {
            final Path configFile = this.configChannelDir.resolve("global.conf");
            final ConfigChatChannel configChannel = this.injector.getInstance(ConfigChatChannel.class);
            final ConfigurationLoader<?> loader = this.configFactory.configurationLoader(FileUtil.mkParentDirs(configFile));
            final ConfigurationNode node = loader.createNode();
            node.set(ConfigChatChannel.class, configChannel);
            loader.save(node);
        } catch (final IOException exception) {
            throw Exceptions.rethrow(exception);
        }
    }

    private @Nullable ChatChannel loadChannel(final Path channelFile) {
        final ConfigurationLoader<?> loader = this.configFactory.configurationLoader(channelFile);

        try {
            final ConfigurationNode loaded = updateNode(loader.load());
            loader.save(loaded);
            return MAPPER.load(loaded);
        } catch (final ConfigurateException exception) {
            this.logger.warn("Failed to load channel from file '{}'", channelFile, exception);
        }

        return null;
    }

    private void sendMessageInChannelAsPlayer(
        final CarbonPlayer sender,
        final ChatChannel channel,
        final String plainMessage
    ) {
        final CarbonChatEvent chatEvent = this.prepareAndEmitChatEvent(sender, plainMessage, null, channel);

        if (chatEvent.cancelled()) {
            return;
        }

        for (final Audience recipient : chatEvent.recipients()) {
            Component renderedMessage = chatEvent.message();

            for (final KeyedRenderer renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipient, renderedMessage, chatEvent.message());
            }

            recipient.sendMessage(renderedMessage);
        }
    }

    private void registerChannelCommands(final ChatChannel channel) {
        final CommandManager<Commander> commandManager =
            this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<CommandManager<Commander>>() {}));
        if (!commandManager.isCommandRegistrationAllowed() || commandManager.commandTree().getNamedNode(channel.commandName()) != null) {
            return;
        }

        Command.Builder<Commander> builder = commandManager.commandBuilder(channel.commandName(),
                channel.commandAliases(), commandManager.createDefaultCommandMeta())
            .argument(StringArgument.<Commander>builder("message").greedy().asOptional().build());

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

        final Command<Commander> command = builder.senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final @Nullable ChatChannel chatChannel = this.channel(channelKey);

                if (sender.muted()) {
                    this.carbonMessages.muteCannotSpeak(sender);
                    return;
                }
                if (sender.leftChannels().contains(channelKey) && chatChannel != null) {
                    sender.joinChannel(chatChannel);
                    this.carbonMessages.channelJoined(sender);
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

        final Command<Commander> channelCommand = commandManager.commandBuilder("channel", "ch")
            .literal(channelKey.value())
            .proxies(command)
            .build();

        commandManager.command(channelCommand);
    }

    @Override
    public void register(final ChatChannel channel) {
        this.register(channel, true);
    }

    public void register(final ChatChannel channel, final boolean fireRegisterEvent) {
        this.channelRegistry.register(channel.key(), channel);
        if (channel.shouldRegisterCommands()) {
            this.registerChannelCommands(channel);
        }
        if (fireRegisterEvent) {
            this.eventHandler.emit(new ChannelRegisterEventImpl(this, Set.of(channel.key())));
        }
    }

    @Override
    public @Nullable ChatChannel channel(final Key key) {
        return this.channelRegistry.getOrCreateHolder(key).value();
    }

    public @Nullable ChatChannel channelByValue(final String value) {
        if (value.contains(":")) {
            return this.channel(Key.key(value));
        }

        for (final Key key : this.keys()) {
            if (key.value().equalsIgnoreCase(value)) {
                return this.channel(key);
            }
        }

        return null;
    }

    @Override
    public @NonNull Set<Key> keys() {
        return Collections.unmodifiableSet(this.channelRegistry.keys());
    }

    @Override
    public ChatChannel defaultChannel() {
        return Objects.requireNonNull(this.channel(this.defaultKey));
    }

    @Override
    public Key defaultKey() {
        return this.defaultKey;
    }

    @Override
    public ChatChannel keyOrDefault(final Key key) {
        final @Nullable ChatChannel channel = this.channel(key);

        if (channel != null) {
            return channel;
        }

        return this.defaultChannel();
    }

    @Override
    public void allKeys(final Consumer<Key> action) {
        for (final Key key : this.channelRegistry.keys()) {
            action.accept(key);
        }
        this.eventHandler.subscribe(
            CarbonChannelRegisterEvent.class,
            event -> {
                for (final Key key : event.registered()) {
                    action.accept(key);
                }
            }
        );
    }

}
