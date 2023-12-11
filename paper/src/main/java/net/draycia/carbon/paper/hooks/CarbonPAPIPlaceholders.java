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
package net.draycia.carbon.paper.hooks;

import com.google.inject.Inject;
import java.util.Map;
import java.util.function.Function;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonPAPIPlaceholders extends PlaceholderExpansion {

    private final UserManager<?> userManager;
    private final ChannelRegistry channels;
    private final JavaPlugin plugin;
    private final Map<String, Function<OfflinePlayer, Component>> componentResolvers;
    private final Map<String, Function<OfflinePlayer, String>> stringResolvers;

    @Inject
    public CarbonPAPIPlaceholders(
        final UserManager<?> userManager,
        final ChannelRegistry channels,
        final JavaPlugin plugin
    ) {
        this.userManager = userManager;
        this.channels = channels;
        this.plugin = plugin;
        this.componentResolvers = Map.of(
            "party", this::partyName,
            "nickname", this::nickname,
            "displayname", this::displayName
        );
        this.stringResolvers = Map.of(
            "channel_key", this::selectedChannelKey
        );
        this.register();
    }

    @Override
    public String getIdentifier() {
        return this.plugin.getName();
    }

    @Override
    public String getAuthor() {
        return "[" + String.join(", ", this.plugin.getPluginMeta().getAuthors()) + "]";
    }

    @Override
    public String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(final OfflinePlayer player, final String params) {
        for (final Map.Entry<String, Function<OfflinePlayer, Component>> entry : this.componentResolvers.entrySet()) {
            if (params.endsWith(entry.getKey())) {
                return mm(entry.getValue().apply(player));
            } else if (params.endsWith(entry.getKey() + "_l")) {
                return legacy(entry.getValue().apply(player));
            }
        }

        for (final Map.Entry<String, Function<OfflinePlayer, String>> entry : this.stringResolvers.entrySet()) {
            if (params.endsWith(entry.getKey())) {
                return entry.getValue().apply(player);
            }
        }

        return null;
    }

    private static String mm(final Component in) {
        return MiniMessage.miniMessage().serialize(in);
    }

    private static String legacy(final Component in) {
        return LegacyComponentSerializer.legacySection().serialize(in);
    }

    private Component partyName(final OfflinePlayer player) {
        final @Nullable Party party = this.userManager.user(player.getUniqueId()).thenCompose(CarbonPlayer::party).join();
        return party == null ? Component.empty() : party.name();
    }

    private Component displayName(final OfflinePlayer player) {
        final CarbonPlayer carbonPlayer = this.userManager.user(player.getUniqueId()).join();
        return carbonPlayer.displayName();
    }

    private Component nickname(final OfflinePlayer player) {
        final CarbonPlayer carbonPlayer = this.userManager.user(player.getUniqueId()).join();
        final @Nullable Component nickname = carbonPlayer.nickname();
        return nickname == null ? Component.text(carbonPlayer.username()) : nickname;
    }

    private String selectedChannelKey(final OfflinePlayer player) {
        final CarbonPlayer carbonPlayer = this.userManager.user(player.getUniqueId()).join();
        final @Nullable ChatChannel selected = carbonPlayer.selectedChannel();
        if (selected != null) {
            return selected.key().asString();
        }
        return this.channels.defaultKey().asString();
    }

}
