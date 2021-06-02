package net.draycia.carbon.common.serialisation.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import java.io.IOException;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;

import static net.kyori.adventure.key.Key.key;

public class ChatChannelSerializerGson extends TypeAdapter<ChatChannel> {

    private final ChannelRegistry registry;

    @Inject
    public ChatChannelSerializerGson(final ChannelRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void write(final JsonWriter out, final ChatChannel value) throws IOException {
        out.value(value.key().asString());
    }

    @Override
    public ChatChannel read(final JsonReader in) throws IOException {
        return this.registry.getOrDefault(key(in.nextString()));
    }

}
