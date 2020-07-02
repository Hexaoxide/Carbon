package net.draycia.simplechat.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;

import java.io.IOException;

public class ChatChannelAdapter extends TypeAdapter<ChatChannel> {

    private SimpleChat simpleChat;

    public ChatChannelAdapter(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Override
    public void write(JsonWriter out, ChatChannel value) throws IOException {
        if (value != null) {
            out.beginObject();
            out.name("name").value(value.getName());
            out.endObject();
        }
    }

    @Override
    public ChatChannel read(JsonReader in) throws IOException {
        ChatChannel channel;

        in.beginObject();

        if (in.hasNext()) {
            String key = in.nextName();
            String value = in.nextString();

            channel = simpleChat.getChannel(value);
        } else {
            channel = simpleChat.getDefaultChannel();
        }

        in.endObject();

        return channel;
    }

}
