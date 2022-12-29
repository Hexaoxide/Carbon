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
package net.draycia.carbon.api.users;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.util.InventorySlot;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
class EmptyCarbonPlayer implements CarbonPlayer {

    @Override
    public double distanceSquaredFrom(final CarbonPlayer other) {
        return 0;
    }

    @Override
    public boolean sameWorldAs(final CarbonPlayer other) {
        return false;
    }

    @Override
    public String username() {
        return "";
    }

    @Override
    public boolean hasCustomDisplayName() {
        return false;
    }

    @Override
    public @Nullable Component displayName() {
        return null;
    }

    @Override
    public void displayName(final @Nullable Component displayName) {

    }

    @Override
    public UUID uuid() {
        return new UUID(0, 0);
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return null;
    }

    @Override
    public @Nullable Locale locale() {
        return null;
    }

    @Override
    public @Nullable ChatChannel selectedChannel() {
        return null;
    }

    @Override
    public void selectedChannel(final @Nullable ChatChannel chatChannel) {

    }

    @Override
    public boolean hasPermission(final String permission) {
        return false;
    }

    @Override
    public String primaryGroup() {
        return "";
    }

    @Override
    public List<String> groups() {
        return List.of();
    }

    @Override
    public boolean muted() {
        return false;
    }

    @Override
    public void muted(final boolean muted) {

    }

    @Override
    public boolean ignoring(final UUID player) {
        return false;
    }

    @Override
    public boolean ignoring(final CarbonPlayer sender) {
        return false;
    }

    @Override
    public void ignoring(final UUID player, final boolean nowIgnoring) {

    }

    @Override
    public void ignoring(final CarbonPlayer player, final boolean nowIgnoring) {

    }

    @Override
    public boolean deafened() {
        return false;
    }

    @Override
    public void deafened(final boolean deafened) {

    }

    @Override
    public boolean spying() {
        return false;
    }

    @Override
    public void spying(final boolean spying) {

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
        return null;
    }

    @Override
    public void whisperReplyTarget(final @Nullable UUID uuid) {

    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return null;
    }

    @Override
    public void lastWhisperTarget(final @Nullable UUID uuid) {

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
    public List<Key> leftChannels() {
        return List.of();
    }

    @Override
    public void joinChannel(final ChatChannel channel) {

    }

    @Override
    public void leaveChannel(final ChatChannel channel) {

    }

    @Override
    public @NotNull Identity identity() {
        return Identity.nil();
    }

}
