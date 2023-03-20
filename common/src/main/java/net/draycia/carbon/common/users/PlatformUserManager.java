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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class PlatformUserManager<C extends WrappedCarbonPlayer> implements UserManagerInternal<C>, SaveOnChange {

    private final UserManagerInternal<CarbonPlayerCommon> wrapped;
    private final SaveOnChange saveOnChange;

    public PlatformUserManager(final UserManagerInternal<CarbonPlayerCommon> wrapped) {
        this.wrapped = wrapped;
        this.saveOnChange = ImposterSaveOnChange.impersonateIfNeeded(wrapped);
    }

    @Override
    public CompletableFuture<C> user(UUID uuid) {
        return this.wrapped.user(uuid).thenApply(this::wrap);
    }

    protected abstract C wrap(CarbonPlayerCommon common);

    @Override
    public int saveDisplayName(final UUID id, final @Nullable Component component) {
        return this.saveOnChange.saveDisplayName(id, component);
    }

    @Override
    public int saveMuted(final UUID id, final boolean muted) {
        return this.saveOnChange.saveMuted(id, muted);
    }

    @Override
    public int saveDeafened(final UUID id, final boolean deafened) {
        return this.saveOnChange.saveDeafened(id, deafened);
    }

    @Override
    public int saveSpying(final UUID id, final boolean spying) {
        return this.saveOnChange.saveSpying(id, spying);
    }

    @Override
    public int saveSelectedChannel(final UUID id, final @Nullable Key selectedChannel) {
        return this.saveOnChange.saveSelectedChannel(id, selectedChannel);
    }

    @Override
    public int saveLastWhisperTarget(final UUID id, final @Nullable UUID lastWhisperTarget) {
        return this.saveOnChange.saveLastWhisperTarget(id, lastWhisperTarget);
    }

    @Override
    public int saveWhisperReplyTarget(final UUID id, final @Nullable UUID whisperReplyTarget) {
        return this.saveOnChange.saveWhisperReplyTarget(id, whisperReplyTarget);
    }

    @Override
    public int addIgnore(final UUID id, final UUID ignoredPlayer) {
        return this.saveOnChange.addIgnore(id, ignoredPlayer);
    }

    @Override
    public int removeIgnore(final UUID id, final UUID ignoredPlayer) {
        return this.saveOnChange.removeIgnore(id, ignoredPlayer);
    }

    @Override
    public int addLeftChannel(final UUID id, final Key channel) {
        return this.saveOnChange.addLeftChannel(id, channel);
    }

    @Override
    public int removeLeftChannel(final UUID id, final Key channel) {
        return this.saveOnChange.removeLeftChannel(id, channel);
    }

    @Override
    public void shutdown() {
        this.wrapped.shutdown();
    }

    @Override
    public CompletableFuture<Void> save(C player) {
        return this.wrapped.save(player.carbonPlayerCommon());
    }

    @Override
    public CompletableFuture<Void> loggedOut(final UUID uuid) {
        return this.wrapped.loggedOut(uuid);
    }

}
