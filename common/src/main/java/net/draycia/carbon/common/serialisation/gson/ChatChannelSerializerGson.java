package net.draycia.carbon.common.serialisation.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import java.io.IOException;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;

import static net.kyori.adventure.key.Key.key;

@DefaultQualifier(NonNull.class)
public class ChatChannelSerializerGson extends TypeAdapter<ChatChannel> {

    private final ChannelRegistry registry;

    @Inject
    public ChatChannelSerializerGson(final ChannelRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void write(final JsonWriter out, final @Nullable ChatChannel value) throws IOException {
        if (value == null) {
            out.value((String) null);
        } else {
            out.value(value.key().asString());
        }
    }

    @Override
    public @Nullable ChatChannel read(final JsonReader in) throws IOException {
        @Subst("namespace:value") final @Nullable String channelName = in.nextString();

        if (channelName != null) {
            return this.registry.get(key(channelName));
        }

        return null;
    }

}
