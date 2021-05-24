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
import java.util.Map;
import java.util.Properties;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonMessageSource implements IMessageSource<String, Audience> {

    private final Properties properties;

    @Inject
    CarbonMessageSource(
        final @ForCarbon Path dataDirectory,
        final PrimaryConfig primaryConfig
    ) throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        this.properties = new Properties();

        final String fileName = primaryConfig.translationFile();
        final Path file = dataDirectory.resolve(fileName);

        if (Files.isRegularFile(file)) {
            final InputStream inputStream = Files.newInputStream(file);
            try (final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                this.properties.load(reader);
            }
        }

        boolean write = !Files.isRegularFile(file);

        try (final InputStream stream = CarbonChat.class.getResourceAsStream("/" + fileName)) {
            final Properties packaged = new Properties();
            packaged.load(stream);

            for (final Map.Entry<Object, Object> entry : packaged.entrySet()) {
                write |= this.properties.putIfAbsent(entry.getKey(), entry.getValue()) == null;
            }
        }

        if (write) {
            final BufferedWriter outputStream = Files.newBufferedWriter(file);
            this.properties.store(outputStream, null);
        }
    }

    @Override
    public String message(final String key, final Audience receiver) {
        final String value = this.properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("No message mapping for key " + key);
        }

        return value;
    }

}
