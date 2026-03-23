/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.chat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record PlaceholderResolutionSnapshot(
    String senderMessagePlaceholderOutput,
    Map<String, String> senderFormatPlaceholders,
    Map<UUID, Map<String, String>> recipientRelationalPlaceholders,
    Set<String> resolvedTokens,
    Set<String> relationalTokens
) {

    public static PlaceholderResolutionSnapshot empty(final String senderMessagePlaceholderOutput) {
        return new PlaceholderResolutionSnapshot(senderMessagePlaceholderOutput, Map.of(), Map.of(), Set.of(), Set.of());
    }

    public PlaceholderResolutionSnapshot {
        senderFormatPlaceholders = Map.copyOf(senderFormatPlaceholders);
        recipientRelationalPlaceholders = recipientRelationalPlaceholders.entrySet().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
        resolvedTokens = Set.copyOf(resolvedTokens);
        relationalTokens = Set.copyOf(relationalTokens);
    }

    public Map<String, String> relationalPlaceholdersFor(final UUID recipientUuid) {
        return this.recipientRelationalPlaceholders.getOrDefault(recipientUuid, Map.of());
    }

}
