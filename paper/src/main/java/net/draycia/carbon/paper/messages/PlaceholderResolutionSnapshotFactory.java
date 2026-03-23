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
package net.draycia.carbon.paper.messages;

import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.chat.PlaceholderResolutionSnapshot;
import net.draycia.carbon.paper.CarbonChatPaper;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class PlaceholderResolutionSnapshotFactory {

    private final PlaceholderValueCache placeholderValueCache;

    @com.google.inject.Inject
    private PlaceholderResolutionSnapshotFactory(final PlaceholderValueCache placeholderValueCache) {
        this.placeholderValueCache = placeholderValueCache;
    }

    public PlaceholderResolutionSnapshot create(
        final CarbonPlayer sender,
        final ChatChannel channel,
        final Collection<? extends Audience> recipients,
        final String senderMessagePlaceholderOutput
    ) {
        if (!CarbonChatPaper.papiLoaded() || !sender.online()) {
            return PlaceholderResolutionSnapshot.empty(senderMessagePlaceholderOutput);
        }

        final @Nullable Player senderBukkitPlayer = Bukkit.getPlayer(sender.uuid());
        if (senderBukkitPlayer == null || !(channel instanceof ConfigChatChannel configChannel)) {
            return PlaceholderResolutionSnapshot.empty(senderMessagePlaceholderOutput);
        }

        final Set<String> formatTokens = new LinkedHashSet<>();
        for (final Audience recipient : recipients) {
            formatTokens.addAll(PlaceholderAPIMiniMessageParser.collectPlaceholderTokens(configChannel.messageFormat(sender, recipient)));
        }

        if (formatTokens.isEmpty()) {
            return PlaceholderResolutionSnapshot.empty(senderMessagePlaceholderOutput);
        }

        final Map<String, String> senderFormatPlaceholders = new LinkedHashMap<>();
        for (final String token : formatTokens) {
            senderFormatPlaceholders.put(token, this.placeholderValueCache.placeholder(senderBukkitPlayer, token));
        }

        final Map<UUID, Map<String, String>> recipientRelationalPlaceholders = new HashMap<>();
        final Set<String> relationalTokens = new LinkedHashSet<>();
        for (final Audience recipient : recipients) {
            if (!(recipient instanceof CarbonPlayer carbonRecipient) || !carbonRecipient.online()) {
                continue;
            }

            final @Nullable Player recipientBukkitPlayer = Bukkit.getPlayer(carbonRecipient.uuid());
            if (recipientBukkitPlayer == null) {
                continue;
            }

            final Map<String, String> recipientOverrides = new LinkedHashMap<>();
            for (final String token : formatTokens) {
                final String relationalValue = PlaceholderAPI.setPlaceholders(
                    senderBukkitPlayer,
                    PlaceholderAPI.setRelationalPlaceholders(recipientBukkitPlayer, senderBukkitPlayer, token)
                );
                if (!Objects.equals(relationalValue, senderFormatPlaceholders.get(token))) {
                    recipientOverrides.put(token, relationalValue);
                    relationalTokens.add(token);
                }
            }

            if (!recipientOverrides.isEmpty()) {
                recipientRelationalPlaceholders.put(carbonRecipient.uuid(), recipientOverrides);
            }
        }

        return new PlaceholderResolutionSnapshot(
            senderMessagePlaceholderOutput,
            senderFormatPlaceholders,
            recipientRelationalPlaceholders,
            formatTokens,
            relationalTokens
        );
    }

}
