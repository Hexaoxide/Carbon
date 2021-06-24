package net.draycia.carbon.common.serialisation.gson;

import java.lang.reflect.Type;
import java.util.Locale;
import net.kyori.adventure.translation.Translator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class LocaleSerializerConfigurate implements TypeSerializer<Locale> {

    @Override
    public Locale deserialize(Type type, ConfigurationNode node) throws SerializationException {
        final String value = node.getString();

        if (value == null) {
            throw new SerializationException("value cannot be null!");
        }

        return Translator.parseLocale(value);
    }

    @Override
    public void serialize(Type type, @Nullable Locale obj, ConfigurationNode node) throws SerializationException {
        node.set(obj.toLanguageTag());
    }

}
