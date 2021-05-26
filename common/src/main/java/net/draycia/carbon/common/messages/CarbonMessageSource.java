package net.draycia.carbon.common.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.moonshine.message.IMessageSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonMessageSource implements IMessageSource<String, Audience> {

    private final Properties defaultMessages;
    private final Map<Locale, Properties> locales = new HashMap<>();

    @Inject
    CarbonMessageSource(
        final @ForCarbon Path dataDirectory,
        final PrimaryConfig primaryConfig
    ) throws IOException {
        // TODO: load in all locales in the "locale" folder
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        this.defaultMessages = new Properties();

        final String fileName = primaryConfig.translationFile();
        final Path file = dataDirectory.resolve(fileName);

        if (Files.isRegularFile(file)) {
            final InputStream inputStream = Files.newInputStream(file);
            try (final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                this.defaultMessages.load(reader);
            }
        }

        boolean write = !Files.isRegularFile(file);

        try (final @Nullable InputStream stream = CarbonChat.class.getResourceAsStream("/locale/" + fileName)) {
            if (stream != null) {
                final Properties packaged = new Properties();
                packaged.load(stream);

                for (final Map.Entry<Object, Object> entry : packaged.entrySet()) {
                    write |= this.defaultMessages.putIfAbsent(entry.getKey(), entry.getValue()) == null;
                }
            }
        }

        if (write) {
            final BufferedWriter outputStream = Files.newBufferedWriter(file);
            this.defaultMessages.store(outputStream, null);
        }
    }

    @Override
    public String message(final String key, final Audience receiver) {
        if (receiver instanceof CarbonPlayer player) {
            return this.forPlayer(key, player);
        } else {
            return this.forAudience(key, receiver);
        }
    }

    private String forPlayer(final String key, final CarbonPlayer player) {
        if (player.locale() != null) {
            final var properties = this.locales.get(player.locale());

            if (properties != null) {
                final var message = properties.getProperty(key);

                if (message != null) {
                    return message;
                }
            }
        }

        return forAudience(key, player);
    }

    private String forAudience(final String key, final Audience audience) {
        final String value = this.defaultMessages.getProperty(key);

        if (value == null) {
            throw new IllegalStateException("No message mapping for key " + key);
        }

        return value;
    }

}
