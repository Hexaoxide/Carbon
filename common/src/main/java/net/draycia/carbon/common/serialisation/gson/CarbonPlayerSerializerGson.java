package net.draycia.carbon.common.serialisation.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.text.Component;

public class CarbonPlayerSerializerGson implements JsonSerializer<CarbonPlayer>, JsonDeserializer<CarbonPlayer> {

    @Override
    public CarbonPlayer deserialize(
        final JsonElement jsonElement,
        final Type type,
        final JsonDeserializationContext context
    ) throws JsonParseException {
        final JsonObject object = jsonElement.getAsJsonObject();

        final Component displayName = context.deserialize(object.get("displayName"), Component.class);
        final ChatChannel selectedChannel = context.deserialize(object.get("selectedChannel"), ChatChannel.class);
        final String username = object.get("username").getAsString();
        final UUID uuid = context.deserialize(object.get("uuid"), UUID.class);

        return new CarbonPlayerCommon(displayName, selectedChannel, username, uuid);
    }

    @Override
    public JsonElement serialize(
        final CarbonPlayer player,
        final Type type,
        final JsonSerializationContext context
    ) {
        final JsonObject object = new JsonObject();

        object.add("displayName", context.serialize(player.displayName()));
        object.add("selectedChannel", context.serialize(player.selectedChannel()));
        object.add("username", context.serialize(player.username()));
        object.add("uuid", context.serialize(player.uuid()));

        return object;
    }

}
