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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.InventorySlot;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public class CarbonPlayerCommon implements CarbonPlayer, ForwardingAudience.Single {

    protected boolean muted = false;
    protected boolean deafened = false;

    protected @Nullable Key selectedChannel = null;

    // All players have these
    protected transient @MonotonicNonNull String username = null;
    protected @MonotonicNonNull UUID uuid;

    // Display information
    protected @Nullable Component displayName = null;

    // Whispers
    protected transient @Nullable UUID lastWhisperTarget = null;
    protected transient @Nullable UUID whisperReplyTarget = null;

    // Administrative
    protected boolean spying = false;

    // Punishments
    protected List<UUID> ignoredPlayers = new ArrayList<>();

    public CarbonPlayerCommon(
        final boolean muted,
        final boolean deafened,
        final Key selectedChannel,
        final String username,
        final UUID uuid,
        final Component displayName,
        final UUID lastWhisperTarget,
        final UUID whisperReplyTarget,
        final boolean spying
    ) {
        this.muted = muted;
        this.deafened = deafened;
        this.selectedChannel = selectedChannel;
        this.username = username;
        this.uuid = uuid;
        this.displayName = displayName;
        this.lastWhisperTarget = lastWhisperTarget;
        this.whisperReplyTarget = whisperReplyTarget;
        this.spying = spying;
    }

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
    public boolean hasPermission(final String permission) {
        return false;
    }

    @Override
    public String primaryGroup() {
        return "default";
    }

    @Override
    public List<String> groups() {
        return List.of("default");
    }

    @Override
    public boolean muted() {
        return this.muted;
    }

    @Override
    public void muted(final boolean muted) {
        this.muted = muted;
    }

    @Override
    public boolean ignoring(final UUID player) {
        return this.ignoredPlayers.contains(player);
    }

    @Override
    public boolean ignoring(final CarbonPlayer player) {
        return this.ignoring(player.uuid());
    }

    @Override
    public void ignoring(final UUID player, final boolean nowIgnoring) {
        if (nowIgnoring) {
            if (!this.ignoredPlayers.contains(player)) {
                this.ignoredPlayers.add(player);
            }
        } else {
            this.ignoredPlayers.remove(player);
        }
    }

    @Override
    public void ignoring(final CarbonPlayer player, final boolean nowIgnoring) {
        this.ignoring(player.uuid(), nowIgnoring);
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
    public boolean speechPermitted(final String message) {
        return true;
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
        return CarbonChatProvider.carbonChat().channelRegistry().get(this.selectedChannel);
    }

    @Override
    public void selectedChannel(final @Nullable ChatChannel chatChannel) {
        this.selectedChannel = chatChannel.key();
    }

    @Override
    public String username() {
        if (this.username == null) {
            this.username = Objects.requireNonNull(CarbonChatProvider.carbonChat().server()
                .resolveName(this.uuid).join());
        }

        return this.username;
    }

    public void username(final String username) {
        this.username = username;
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
