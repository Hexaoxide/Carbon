/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.punishments.MuteEntry;
import net.draycia.carbon.api.util.InventorySlot;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public class CarbonPlayerCommon implements CarbonPlayer, ForwardingAudience.Single {

    protected boolean deafened = false;
    protected @Nullable ChatChannel selectedChannel = null;

    // All players have these
    protected @MonotonicNonNull String username;
    protected @MonotonicNonNull UUID uuid;

    // Display information
    protected @Nullable Component displayName = null;
    protected @Nullable Component temporaryDisplayName = null;
    protected long temporaryDisplayNameExpiration = -1;

    // Whispers
    protected transient @Nullable UUID lastWhisperTarget = null;
    protected transient @Nullable UUID whisperReplyTarget = null;

    // Administrative
    protected boolean spying = false;

    // Punishments
    protected List<MuteEntry> muteEntries = new ArrayList<>();
    protected List<UUID> ignoredPlayers = new ArrayList<>();

    public CarbonPlayerCommon(
        final String username,
        final UUID uuid
    ) {
        this.username = username;
        this.uuid = uuid;
    }

    public CarbonPlayerCommon() {

    }

    @Override
    public @NotNull Audience audience() {
        return Audience.empty();
    }

    protected CarbonPlayer carbonPlayer() {
        return this;
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return null;
    }

    @Override
    public @Nullable Component displayName() {
        if (this.displayName != null) {
            return this.displayName;
        }

        return null;
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.displayName = displayName;
    }

    @Override
    public void temporaryDisplayName(final @Nullable Component temporaryDisplayName, final long expirationEpoch) {
        // TODO: see why these aren't persisting
        this.temporaryDisplayName = temporaryDisplayName;
        this.temporaryDisplayNameExpiration = expirationEpoch;
    }

    @Override
    public @Nullable Component temporaryDisplayName() {
        return this.temporaryDisplayName;
    }

    @Override
    public long temporaryDisplayNameExpiration() {
        return this.temporaryDisplayNameExpiration;
    }

    @Override
    public boolean hasActiveTemporaryDisplayName() {
        return this.temporaryDisplayName != null
            && (this.temporaryDisplayNameExpiration == -1
            || this.temporaryDisplayNameExpiration > System.currentTimeMillis());
    }

    @Override
    public boolean hasPermission(final String permission) {
        return false;
    }

    @Override
    public String primaryGroup() {
        return "default"; // TODO: implement
    }

    @Override
    public List<String> groups() {
        return List.of("default"); // TODO: implement
    }

    @Override
    public List<MuteEntry> muteEntries() {
        return Collections.unmodifiableList(this.muteEntries);
    }

    @Override
    public boolean muted(final ChatChannel chatChannel) {
        for (final var muteEntry : this.muteEntries) {
            if (!muteEntry.valid()) {
                continue;
            }

            if (muteEntry.channel() == null || chatChannel.key().equals(muteEntry.channel())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable MuteEntry addMuteEntry(
        final @Nullable ChatChannel chatChannel,
        final boolean muted,
        final @Nullable UUID cause,
        final long duration,
        final @Nullable String reason
    ) {
        if (muted) {
            final var muteEntry = new MuteEntry(System.currentTimeMillis(), cause, duration,
                reason, chatChannel != null ? chatChannel.key() : null, UUID.randomUUID());
            this.muteEntries.add(muteEntry);
            return muteEntry;
        } else {
            return null;
        }
    }

    @Override
    public boolean ignoring(final CarbonPlayer sender) {
        return this.ignoredPlayers.contains(sender.uuid());
    }

    @Override
    public void ignoring(final CarbonPlayer player, final boolean nowIgnoring) {
        if (nowIgnoring) {
            this.ignoredPlayers.add(player.uuid());
        } else {
            this.ignoredPlayers.remove(player.uuid());
        }
    }

    @Override
    public boolean deafened() {
        return this.deafened;
    }

    @Override
    public void deafened(final boolean deafened) {
        this.deafened = deafened;
    }

    @Override
    public boolean spying() {
        return this.spying;
    }

    @Override
    public void spying(final boolean spying) {
        this.spying = spying;
    }

    @Override
    public void sendMessageAsPlayer(final String message) {

    }

    @Override
    public boolean online() {
        return false;
    }

    @Override
    public @Nullable UUID whisperReplyTarget() {
        return this.whisperReplyTarget;
    }

    @Override
    public void whisperReplyTarget(final @Nullable UUID uuid) {
        this.whisperReplyTarget = uuid;
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.lastWhisperTarget;
    }

    @Override
    public void lastWhisperTarget(final @Nullable UUID uuid) {
        this.lastWhisperTarget = uuid;
    }

    @Override
    public boolean vanished() {
        return false;
    }

    @Override
    public boolean awareOf(final CarbonPlayer other) {
        return true;
    }

    @Override
    public Identity identity() {
        return Identity.identity(this.uuid);
    }

    @Override
    public @Nullable Locale locale() {
        return Locale.getDefault();
    }

    @Override
    public @Nullable ChatChannel selectedChannel() {
        return this.selectedChannel;
    }

    @Override
    public void selectedChannel(final @Nullable ChatChannel chatChannel) {
        this.selectedChannel = chatChannel;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public boolean hasCustomDisplayName() {
        return this.displayName != null;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

}
