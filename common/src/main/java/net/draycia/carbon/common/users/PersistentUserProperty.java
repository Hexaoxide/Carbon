/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.users;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.ApiStatus;

@DefaultQualifier(NonNull.class)
public final class PersistentUserProperty<T> {

    private @Nullable T value;
    private boolean changed = false;

    public PersistentUserProperty(final @Nullable T value) {
        this.value = value;
    }

    /**
     * Set the value without setting the changed flag. This is a hack.
     *
     * @param value value
     */
    @ApiStatus.Internal
    public void setInternal(final @Nullable T value) {
        this.value = value;
    }

    public void set(final @Nullable T value) {
        if (Objects.equals(value, this.value)) {
            return;
        }
        this.value = value;
        this.changed = true;
    }

    public T get() {
        return Objects.requireNonNull(this.value, "value required but not present");
    }

    public boolean hasValue() {
        return this.value != null;
    }

    public @Nullable T orNull() {
        return this.value;
    }

    public boolean changed() {
        return this.changed;
    }

    public static <T> PersistentUserProperty<T> of(final @Nullable T value) {
        return new PersistentUserProperty<>(value);
    }

    public static <T> PersistentUserProperty<T> empty() {
        return new PersistentUserProperty<>(null);
    }

    public static final class Serializer implements JsonSerializer<PersistentUserProperty<?>>, JsonDeserializer<PersistentUserProperty<?>> {

        @Override
        public PersistentUserProperty<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final Type propType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
            return new PersistentUserProperty<>(context.deserialize(json, propType));
        }

        @Override
        public JsonElement serialize(final PersistentUserProperty<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
            final Type propType = ((ParameterizedType) typeOfSrc).getActualTypeArguments()[0];
            return context.serialize(src.orNull(), propType);
        }

    }

}
