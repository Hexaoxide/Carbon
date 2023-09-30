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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.api.util.InventorySlot;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public class ConsoleCarbonPlayer implements CarbonPlayer, ForwardingAudience.Single {

    private final Audience audience;

    public ConsoleCarbonPlayer(final Audience audience) {
        this.audience = audience;
    }

    @Override
    public @NotNull Audience audience() {
        return this.audience;
    }

    @Override
    public double distanceSquaredFrom(final CarbonPlayer other) {
        return 0;
    }

    @Override
    public boolean sameWorldAs(final CarbonPlayer other) {
        return true;
    }

    @Override
    public String username() {
        return "Console";
    }

    @Override
    public Component displayName() {
        return Component.text(this.username());
    }

    @Override
    public boolean hasNickname() {
        return false;
    }

    @Override
    public @Nullable Component nickname() {
        return null;
    }

    @Override
    public void nickname(final @Nullable Component nickname) {

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
        return true;
    }

    @Override
    public String primaryGroup() {
        return "console_sender";
    }

    @Override
    public List<String> groups() {
        return List.of("console_sender");
    }

    @Override
    public boolean muted() {
        return false;
    }

    @Override
    public void muted(final boolean muted) {

    }

    @Override
    public Set<UUID> ignoring() {
        return Collections.emptySet();
    }

    @Override
    public boolean ignoring(final UUID player) {
        return false;
    }

    @Override
    public boolean ignoring(final CarbonPlayer player) {
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
        return true;
    }

    @Override
    public void spying(final boolean spying) {

    }

    @Override
    public boolean ignoringDirectMessages() {
        return false;
    }

    @Override
    public void ignoringDirectMessages(final boolean ignoring) {

    }

    @Override
    public void sendMessageAsPlayer(final String message) {

    }

    @Override
    public boolean online() {
        return true;
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
    public CompletableFuture<@Nullable Party> party() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull Identity identity() {
        return Identity.nil();
    }

    @Override
    public ChannelMessage channelForMessage(final Component message) {
        return new ChannelMessage(message, null);
    }

}
