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
package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.seiama.registry.Holder;
import com.seiama.registry.Registry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChannelRegisterEvent;
import net.draycia.carbon.api.event.events.ChannelSwitchEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.ParserFactory;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.SignedGreedyStringParser;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.event.events.CarbonReloadEvent;
import net.draycia.carbon.common.event.events.ChannelRegisterEventImpl;
import net.draycia.carbon.common.event.events.ChannelSwitchEventImpl;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.ConsoleCarbonPlayer;
import net.draycia.carbon.common.util.Exceptions;
import net.draycia.carbon.common.util.FileUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonChannelRegistry extends ChatListenerInternal implements ChannelRegistry {

    private final Path configChannelDir;
    private final Injector injector;
    private final Logger logger;
    private final ConfigManager config;
    private @MonotonicNonNull Key defaultKey;
    private final CarbonMessages carbonMessages;
    private final CarbonEventHandler eventHandler;
    private final ParserFactory parserFactory;
    private final Map<String, SpecialHandler<?>> handlers = new HashMap<>();

    private record SpecialHandler<T extends ConfigChatChannel>(Class<T> cls, Supplier<T> defaultSupplier) {}

    public <T extends ConfigChatChannel> void registerSpecialConfigChannel(final String fileName, final Class<T> type) {
        if (this.handlers.containsKey(fileName)) {
            throw new IllegalStateException("Attempting to register duplicate entry (existing: " + this.handlers.get(fileName)
                + ", new: " + type + ") for key " + fileName);
        }
        this.handlers.put(fileName, new SpecialHandler<>(type, () -> this.injector.getInstance(type)));
    }

    private volatile Registry<Key, ChatChannel> channelRegistry = Registry.create();
    private final Set<Key> configChannels = ConcurrentHashMap.newKeySet();
    //
    // private final BiMap<Key, ChatChannel> channelMap = Maps.synchronizedBiMap(HashBiMap.create());

    @Inject
    public CarbonChannelRegistry(
        @DataDirectory final Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final ConfigManager config,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler events,
        final ParserFactory parserFactory
    ) {
        super(events, carbonMessages, config);
        this.configChannelDir = dataDirectory.resolve("channels");
        this.injector = injector;
        this.logger = logger;
        this.config = config;
        this.carbonMessages = carbonMessages;
        this.eventHandler = events;
        this.parserFactory = parserFactory;

        if (config.primaryConfig().partyChat().enabled) {
            this.registerSpecialConfigChannel(PartyChatChannel.FILE_NAME, PartyChatChannel.class);
        }

        events.subscribe(CarbonReloadEvent.class, -99, true, event -> this.reloadConfigChannels());
    }

    public static ConfigurationTransformation.Versioned configChatChannelUpgrader() {
        // final ConfigurationTransformation initial;

        return ConfigurationTransformation.versionedBuilder()
            .versionKey(ConfigManager.CONFIG_VERSION_KEY)
            // .addVersion(0, initial)
            .build();
    }

    public static <N extends ConfigurationNode> N upgradeConfigChatChannelNode(final N node) throws ConfigurateException {
        if (true) {
            // No transformations yet!
            return node;
        }

        if (!node.virtual()) { // we only want to migrate existing data
            final ConfigurationTransformation.Versioned upgrader = configChatChannelUpgrader();
            final int from = upgrader.version(node);
            upgrader.apply(node);
            final int to = upgrader.version(node);

            ConfigManager.configVersionComment(node, upgrader);

            if (from != to) { // we might not have made any changes
                // TODO: use logger
                //CarbonChatProvider.carbonChat().logger().info("Updated config schema from " + from + " to " + to);
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
        this.logger.info("Loading config channels...");
        this.defaultKey = this.config.primaryConfig().defaultChannel();

        this.saveSpecialDefaults();

        List<Path> channelConfigs = FileUtil.listDirectoryEntries(this.configChannelDir, "*.conf");

        final Set<String> channelConfigFileNames = channelConfigs
            .stream()
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(Collectors.toSet());

        final Set<String> expectedHandlerFileNames = this.handlers.keySet();

        if (channelConfigs.size() == this.handlers.size() && channelConfigFileNames.containsAll(expectedHandlerFileNames)) {
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
                this.logger.info("Default channel is [{}]", channelKey);
            }

            if (this.channelRegistry.keys().contains(channelKey)) {
                this.logger.warn("Channel with key [{}] has already been registered, skipping {}", channelKey, channelConfigFile);
                continue;
            }

            this.injector.injectMembers(chatChannel);
            this.configChannels.add(chatChannel.key());
            this.register(chatChannel, false);
        }

        if (this.channel(this.defaultKey) == null) {
            this.logger.warn("No default channel found! Default channel key: [{}]", this.defaultKey());
        }

        final List<String> channelList = new ArrayList<>();

        for (final Key key : this.keys()) {
            channelList.add(key.asString());
        }

        final String channels = String.join(", ", channelList);

        this.logger.info("Registered channels: [{}]", channels);
    }

    private void saveSpecialDefaults() {
        for (final Map.Entry<String, SpecialHandler<?>> e : this.handlers.entrySet()) {
            final Path configFile = this.configChannelDir.resolve(e.getKey());
            if (Files.isRegularFile(configFile)) {
                continue;
            }
            try {
                final ConfigChatChannel configChannel = e.getValue().defaultSupplier().get();
                final ConfigurationLoader<?> loader = this.config.configurationLoader(FileUtil.mkParentDirs(configFile), ConfigManager.extractHeader(e.getValue().cls()));
                final ConfigurationNode node = loader.createNode();
                node.set(e.getValue().cls(), configChannel);
                loader.save(node);
            } catch (final IOException exception) {
                throw Exceptions.rethrow(exception);
            }
        }
    }

    private void saveDefaultChannelConfig() {
        try {
            final Path configFile = this.configChannelDir.resolve("global.conf");
            final ConfigChatChannel configChannel = this.injector.getInstance(ConfigChatChannel.class);
            final ConfigurationLoader<?> loader = this.config.configurationLoader(FileUtil.mkParentDirs(configFile), ConfigManager.extractHeader(ConfigChatChannel.class));
            final ConfigurationNode node = loader.createNode();
            node.set(ConfigChatChannel.class, configChannel);
            loader.save(node);
        } catch (final IOException exception) {
            throw Exceptions.rethrow(exception);
        }
    }

    private @Nullable ChatChannel loadChannel(final Path channelFile) {
        try {
            final @Nullable SpecialHandler<?> special = this.handlers.get(channelFile.getFileName().toString());
            final Class<? extends ConfigChatChannel> type = special == null ? ConfigChatChannel.class : special.cls();

            final ConfigurationLoader<?> loader = this.config.configurationLoader(channelFile, ConfigManager.extractHeader(type));
            final ConfigurationNode loaded = upgradeConfigChatChannelNode(loader.load());
            final @Nullable ConfigChatChannel channel = loaded.get(type);
            if (channel == null) {
                throw new ConfigurateException("Config deserialized to null.");
            }

            loaded.set(type, channel);
            loader.save(loaded);

            return channel;
        } catch (final ConfigurateException exception) {
            this.logger.warn("Failed to load channel from file '{}'", channelFile, exception);
        }

        return null;
    }

    private void sendMessageInChannelAsConsole(
        final Audience sender,
        final ChatChannel channel,
        final String plainMessage
    ) {
        this.sendMessageInChannel(new ConsoleCarbonPlayer(sender), channel, new SignedGreedyStringParser.NonSignedString(plainMessage));
    }

    private void sendMessageInChannel(
        final CarbonPlayer sender,
        final ChatChannel channel,
        final SignedGreedyStringParser.SignedString message
    ) {
        final @Nullable CarbonChatEventImpl chatEvent = this.prepareAndEmitChatEvent(sender, message.string(), message.signedMessage(), channel);

        if (chatEvent == null || chatEvent.cancelled()) {
            return;
        }

        for (final Audience recipient : chatEvent.recipients()) {
            message.sendMessage(recipient, chatEvent.renderFor(recipient));
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
            .optional("message", this.parserFactory.signedGreedyStringParser());

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

        final Command<Commander> command = builder.senderType(Commander.class)
            .handler(handler -> {
                final Commander commander = handler.sender();
                final @Nullable ChatChannel chatChannel = this.channel(channelKey);

                if (!(commander instanceof PlayerCommander playerCommander)) {
                    if (chatChannel != null && handler.contains("message")) {
                        final SignedGreedyStringParser.SignedString message = handler.get("message");

                        // TODO: trigger platform events related to chat
                        this.sendMessageInChannelAsConsole(commander, chatChannel, message.string());
                    }

                    return;
                }

                final var player = playerCommander.carbonPlayer();

                if (player.muted()) {
                    this.carbonMessages.muteCannotSpeak(player);
                    return;
                }
                if (player.leftChannels().contains(channelKey) && chatChannel != null) {
                    player.joinChannel(chatChannel);
                    this.carbonMessages.channelJoined(player);
                }
                if (handler.contains("message")) {
                    final SignedGreedyStringParser.SignedString message = handler.get("message");

                    // TODO: trigger platform events related to chat
                    this.sendMessageInChannel(player, chatChannel, message);
                } else {
                    final ChannelSwitchEvent switchEvent = new ChannelSwitchEventImpl(player, chatChannel);
                    this.eventHandler.emit(switchEvent);

                    player.selectedChannel(switchEvent.channel());
                    this.carbonMessages.changedChannels(player, channelKey.value());
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
        final @Nullable Holder<Key, ChatChannel> holder = this.channelRegistry.getHolder(key);
        return holder == null ? null : holder.value();
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
    public ChatChannel channelOrDefault(final Key key) {
        final @Nullable ChatChannel channel = this.channel(key);

        if (channel != null) {
            return channel;
        }

        return this.defaultChannel();
    }

    @Override
    public ChatChannel channelOrThrow(final Key key) {
        final @Nullable ChatChannel channel = this.channel(key);
        if (channel != null) {
            return channel;
        }
        throw new NoSuchElementException("No channel registered with key '" + key.asString() + "'");
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
