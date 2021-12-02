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
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.InventorySlot;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public abstract class WrappedCarbonPlayer implements CarbonPlayer {

    public abstract CarbonPlayerCommon carbonPlayerCommon();

    public User user() {
        return Objects.requireNonNull(LuckPermsProvider.get().getUserManager().getUser(this.uuid()));
    }

    @Override
    public boolean awareOf(final CarbonPlayer other) {
        if (other.vanished()) {
            return this.hasPermission("carbon.whisper.vanished");
        }

        return true;
    }

    @Override
    public boolean ignoring(final CarbonPlayer sender) {
        return this.carbonPlayerCommon().ignoring(sender);
    }

    @Override
    public void ignoring(final CarbonPlayer player, final boolean nowIgnoring) {
        this.carbonPlayerCommon().ignoring(player, nowIgnoring);
    }

    @Override
    public boolean hasPermission(final String permission) {
        final var data = this.user().getCachedData().getPermissionData(this.user().getQueryOptions());
        return data.checkPermission(permission) == Tristate.TRUE;
    }

    @Override
    public String primaryGroup() {
        return this.user().getPrimaryGroup();
    }

    @Override
    public List<String> groups() {
        final var groups = new ArrayList<String>();

        for (final var group : this.user().getInheritedGroups(this.user().getQueryOptions())) {
            groups.add(group.getName());
        }

        return groups;
    }

    @Override
    public String username() {
        return this.carbonPlayerCommon().username();
    }

    @Override
    public boolean hasCustomDisplayName() {
        return this.carbonPlayerCommon().hasCustomDisplayName();
    }

    @Override
    public @Nullable Component displayName() {
        return this.carbonPlayerCommon().displayName();
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.carbonPlayerCommon().displayName(displayName);
    }

    @Override
    public UUID uuid() {
        return this.carbonPlayerCommon().uuid();
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return this.carbonPlayerCommon().createItemHoverComponent(slot);
    }

    @Override
    public @Nullable Locale locale() {
        return this.carbonPlayerCommon().locale();
    }

    @Override
    public @Nullable ChatChannel selectedChannel() {
        return this.carbonPlayerCommon().selectedChannel();
    }

    @Override
    public void selectedChannel(final @Nullable ChatChannel chatChannel) {
        this.carbonPlayerCommon().selectedChannel(chatChannel);
    }

    @Override
    public boolean muted() {
        return this.carbonPlayerCommon().muted();
    }

    @Override
    public void muted(final boolean muted) {
        this.carbonPlayerCommon().muted(muted);
    }

    @Override
    public boolean deafened() {
        return this.carbonPlayerCommon().deafened();
    }

    @Override
    public void deafened(final boolean deafened) {
        this.carbonPlayerCommon().deafened(deafened);
    }

    @Override
    public boolean spying() {
        return this.carbonPlayerCommon().spying();
    }

    @Override
    public void spying(final boolean spying) {
        this.carbonPlayerCommon().spying(spying);
    }

    @Override
    public void sendMessageAsPlayer(final String message) {
        this.carbonPlayerCommon().sendMessageAsPlayer(message);
    }

    @Override
    public boolean online() {
        return this.carbonPlayerCommon().online();
    }

    @Override
    public @Nullable UUID whisperReplyTarget() {
        return this.carbonPlayerCommon().whisperReplyTarget();
    }

    @Override
    public void whisperReplyTarget(final @Nullable UUID uuid) {
        this.carbonPlayerCommon().whisperReplyTarget(uuid);
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.carbonPlayerCommon().lastWhisperTarget();
    }

    @Override
    public void lastWhisperTarget(final @Nullable UUID uuid) {
        this.carbonPlayerCommon().lastWhisperTarget(uuid);
    }

    @Override
    public @NotNull Identity identity() {
        return this.carbonPlayerCommon().identity();
    }

}
