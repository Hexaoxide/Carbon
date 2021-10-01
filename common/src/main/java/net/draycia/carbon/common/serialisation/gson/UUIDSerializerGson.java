package net.draycia.carbon.common.serialisation.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class UUIDSerializerGson extends TypeAdapter<UUID> {

    @Override
    public void write(final JsonWriter jsonWriter, final @Nullable UUID uuid) throws IOException {
        if (uuid != null) {
            jsonWriter.value(uuid.toString());
        } else {
            jsonWriter.value((String)null);
        }
    }

    @Override
    public UUID read(final JsonReader jsonReader) throws IOException {
        return UUID.fromString(jsonReader.nextString());
    }

}
