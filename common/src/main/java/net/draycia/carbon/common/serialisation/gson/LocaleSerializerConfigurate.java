package net.draycia.carbon.common.serialisation.gson;

import com.google.inject.Inject;
import java.lang.reflect.Type;
import java.util.Locale;
import net.kyori.adventure.translation.Translator;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public class LocaleSerializerConfigurate implements TypeSerializer<Locale> {

    private final Logger logger;

    @Inject
    public LocaleSerializerConfigurate(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public Locale deserialize(final Type type, final ConfigurationNode node) {
        final @Nullable String value = node.getString();

        if (value == null) {
            this.logger.warn("value null for locale! defaulting to en_US");
            return Locale.ENGLISH;
        }

        return requireNonNull(Translator.parseLocale(value), "value locale cannot be null!");
    }

    @Override
    public void serialize(final Type type, @Nullable final Locale obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.set(obj.toLanguageTag());
        }
    }

}
