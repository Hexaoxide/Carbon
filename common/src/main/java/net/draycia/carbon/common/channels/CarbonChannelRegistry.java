package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.config.ConfigLoader;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.kyori.adventure.key.Key;
import net.kyori.registry.DefaultedRegistry;
import net.kyori.registry.RegistryImpl;
import org.apache.logging.log4j.Logger;
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

    private final ConfigLoader configLoader;
    private final Path dataDirectory;
    private final Injector injector;
    private final Logger logger;
    private final PrimaryConfig primaryConfig;

    private Key defaultKey;
    private ChatChannel basicChannel;

    @Inject
    public CarbonChannelRegistry(
        final ConfigLoader configLoader,
        @ForCarbon final Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final PrimaryConfig primaryConfig
    ) {
        this.configLoader = configLoader;
        this.dataDirectory = dataDirectory;
        this.injector = injector;
        this.logger = logger;
        this.primaryConfig = primaryConfig;
    }

    public void loadChannels() {
        final Path channelDirectory = dataDirectory.resolve("channels");
        this.basicChannel = injector.getInstance(BasicChatChannel.class);
        this.defaultKey = this.primaryConfig.defaultChannel();

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
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            return;
        }

        try (Stream<Path> paths = Files.walk(channelDirectory)) {
            paths.forEach(path -> {
                final String fileName = path.getFileName().toString();

                if (fileName.endsWith(".conf")) {
                    final @Nullable ChatChannel channel = loadChannel(path);

                    if (channel != null) {
                        logger.info("Registering channel with key [" + channel.key() + "]");
                        register(channel.key(), channel);
                    }
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static ObjectMapper<ConfigChatChannel> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.factory().get(ConfigChatChannel.class);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public @Nullable ChatChannel loadChannel(final Path channelFile) {
        final ConfigurationLoader<?> loader = this.configLoader.configurationLoader(channelFile, true);

        try {
            return MAPPER.load(loader.load());
        } catch (ConfigurateException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public @NonNull ChatChannel defaultValue() {
        return Objects.requireNonNullElse(this.get(this.defaultKey), this.basicChannel);
    }

    @Override
    public @NonNull Key defaultKey() {
        return this.defaultKey;
    }

    @Override
    public @NonNull ChatChannel getOrDefault(@NonNull Key key) {
        final @Nullable ChatChannel channel = this.get(key);

        if (channel != null) {
            return channel;
        }

        return this.defaultValue();
    }

}
