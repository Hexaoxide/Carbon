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
package net.draycia.carbon.common.config;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.draycia.carbon.common.integration.Integration;
import net.draycia.carbon.common.util.Exceptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

@DefaultQualifier(NonNull.class)
public final class IntegrationConfigContainer {

    private final Map<String, Object> map = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <C> C config(final Integration.ConfigMeta meta) {
        return (C) Objects.requireNonNull(this.map.get(meta.name()));
    }

    public static final class Serializer implements TypeSerializer<IntegrationConfigContainer> {
        private final Map<String, Type> sections;

        public Serializer(final Set<Integration.ConfigMeta> integrations) {
            this.sections = integrations.stream().collect(Collectors.toMap(Integration.ConfigMeta::name, Integration.ConfigMeta::type));
        }

        @Override
        public IntegrationConfigContainer deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            final IntegrationConfigContainer container = new IntegrationConfigContainer();
            for (final Map.Entry<String, Type> entry : this.sections.entrySet()) {
                final @Nullable Object value = node.node(entry.getKey()).get(entry.getValue());
                Objects.requireNonNull(value);
                container.map.put(entry.getKey(), value);
            }
            return container;
        }

        @Override
        public void serialize(final Type type, final @Nullable IntegrationConfigContainer obj, final ConfigurationNode node) throws SerializationException {
            Objects.requireNonNull(obj);

            for (final Object key : node.childrenMap().keySet()) {
                node.removeChild(key);
            }

            for (final Map.Entry<String, Type> entry : this.sections.entrySet()) {
                node.node(entry.getKey()).set(entry.getValue(), obj.map.get(entry.getKey()));
            }
        }

        @Override
        public IntegrationConfigContainer emptyValue(final Type specificType, final ConfigurationOptions options) {
            final IntegrationConfigContainer container = new IntegrationConfigContainer();
            for (final Map.Entry<String, Type> entry : this.sections.entrySet()) {
                final @Nullable Object value;
                try {
                    value = options.serializers().get(entry.getValue())
                        .deserialize(entry.getValue(), BasicConfigurationNode.root());
                } catch (final Exception e) {
                    throw Exceptions.rethrow(e);
                }
                Objects.requireNonNull(value);
                container.map.put(entry.getKey(), value);
            }
            return container;
        }
    }
}
