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

import com.google.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.InventorySlot;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.common.config.ConfigFactory;
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

    private static final long KEEP_TRANSIENT_LOADS_FOR = Duration.ofMinutes(2).toMillis();

    private transient @MonotonicNonNull @Inject ChannelRegistry channelRegistry;
    private transient @MonotonicNonNull @Inject ProfileResolver profileResolver;
    private transient @MonotonicNonNull @Inject PlatformScheduler scheduler;
    private transient @MonotonicNonNull @Inject ConfigFactory config;
    private volatile transient long transientLoadedSince = -1;

    protected final PersistentUserProperty<Boolean> muted;
    protected final PersistentUserProperty<Boolean> deafened;
    protected final PersistentUserProperty<Key> selectedChannel;

    // All players have these
    protected transient @MonotonicNonNull String username = null;
    protected @MonotonicNonNull UUID uuid;

    // Display information
    protected final PersistentUserProperty<Component> displayName;

    // Whispers
    protected transient @Nullable UUID lastWhisperTarget = null;
    protected transient @Nullable UUID whisperReplyTarget = null;

    // Administrative
    protected final PersistentUserProperty<Boolean> spying;

    // Punishments
    protected final PersistentUserProperty<Set<UUID>> ignoredPlayers;

    protected final PersistentUserProperty<Set<Key>> leftChannels;

    public CarbonPlayerCommon(
        final boolean muted,
        final boolean deafened,
        final @Nullable Key selectedChannel,
        final String username,
        final UUID uuid,
        final @Nullable Component displayName,
        final @Nullable UUID lastWhisperTarget,
        final @Nullable UUID whisperReplyTarget,
        final boolean spying
    ) {
        this.muted = PersistentUserProperty.of(muted);
        this.deafened = PersistentUserProperty.of(deafened);
        this.selectedChannel = PersistentUserProperty.of(selectedChannel);
        this.username = username;
        this.uuid = uuid;
        this.displayName = PersistentUserProperty.of(displayName);
        this.lastWhisperTarget = lastWhisperTarget;
        this.whisperReplyTarget = whisperReplyTarget;
        this.spying = PersistentUserProperty.of(spying);
        this.ignoredPlayers = PersistentUserProperty.of(Collections.emptySet());
        this.leftChannels = PersistentUserProperty.of(Collections.emptySet());
    }

    public CarbonPlayerCommon(
        final @Nullable String username, // will be resolved when requested
        final UUID uuid
    ) {
        this.muted = PersistentUserProperty.of(false);
        this.deafened = PersistentUserProperty.of(false);
        this.selectedChannel = PersistentUserProperty.empty();
        this.displayName = PersistentUserProperty.empty();
        this.spying = PersistentUserProperty.of(false);
        this.ignoredPlayers = PersistentUserProperty.of(Collections.emptySet());
        this.leftChannels = PersistentUserProperty.of(Collections.emptySet());
        this.username = username;
        this.uuid = uuid;
    }

    public CarbonPlayerCommon() {
        this.muted = PersistentUserProperty.of(false);
        this.deafened = PersistentUserProperty.of(false);
        this.selectedChannel = PersistentUserProperty.empty();
        this.displayName = PersistentUserProperty.empty();
        this.spying = PersistentUserProperty.of(false);
        this.ignoredPlayers = PersistentUserProperty.of(Collections.emptySet());
        this.leftChannels = PersistentUserProperty.of(Collections.emptySet());
    }

    public boolean needsSave() {
        return this.properties().anyMatch(PersistentUserProperty::changed);
    }

    private Stream<PersistentUserProperty<?>> properties() {
        return Stream.of(
            this.muted,
            this.deafened,
            this.selectedChannel,
            this.displayName,
            this.spying,
            this.ignoredPlayers,
            this.leftChannels
        );
    }

    public void schedule(final Runnable task) {
        this.scheduler.scheduleForPlayer(this, task);
    }

    public void registerPropertyUpdateListener(final Runnable task) {
        this.properties().forEach(prop -> prop.registerUpdateListener(task));
    }

    @Override
    public @NotNull Audience audience() {
        return Audience.empty();
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return null;
    }

    @Override
    public @Nullable Component displayName() {
        if (!this.config.primaryConfig().useCarbonNicknames()) {
            return null;
        }
        return this.displayName.orNull();
    }

    public @Nullable Component displayNameRaw() {
        return this.displayName.orNull();
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.displayName.set(displayName);
    }

    @Override
    public boolean hasPermission(final String permission) {
        throw new UnsupportedOperationException();
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
        return this.muted.get();
    }

    @Override
    public void muted(final boolean muted) {
        this.muted.set(muted);
    }

    public Set<UUID> ignoredPlayers() {
        return this.ignoredPlayers.get();
    }

    @Override
    public boolean ignoring(final UUID player) {
        return this.ignoredPlayers.get().contains(player);
    }

    @Override
    public boolean ignoring(final CarbonPlayer player) {
        return this.ignoring(player.uuid());
    }

    public void ignoring(final UUID player, final boolean nowIgnoring, final boolean internal) {
        final Set<UUID> newIgnored = new HashSet<>(this.ignoredPlayers.get());
        if (nowIgnoring) {
            newIgnored.add(player);
        } else {
            newIgnored.remove(player);
        }
        if (internal) {
            this.ignoredPlayers.internalSet(Collections.unmodifiableSet(newIgnored));
        } else {
            this.ignoredPlayers.set(Collections.unmodifiableSet(newIgnored));
        }
    }

    @Override
    public void ignoring(final UUID player, final boolean nowIgnoring) {
        this.ignoring(player, nowIgnoring, false);
    }

    @Override
    public void ignoring(final CarbonPlayer player, final boolean nowIgnoring) {
        this.ignoring(player.uuid(), nowIgnoring);
    }

    @Override
    public boolean deafened() {
        return this.deafened.get();
    }

    @Override
    public void deafened(final boolean deafened) {
        this.deafened.set(deafened);
    }

    @Override
    public boolean spying() {
        return this.spying.get();
    }

    @Override
    public void spying(final boolean spying) {
        this.spying.set(spying);
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
    public void whisperReplyTarget(final @Nullable UUID whisperReplyTarget) {
        this.whisperReplyTarget = whisperReplyTarget;
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.lastWhisperTarget;
    }

    @Override
    public void lastWhisperTarget(final @Nullable UUID lastWhisperTarget) {
        this.lastWhisperTarget = lastWhisperTarget;
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
        return List.copyOf(this.leftChannels.get());
    }

    public void joinChannel(final Key key, final boolean internal) {
        final Set<Key> newKeys = new HashSet<>(this.leftChannels.get());
        newKeys.remove(key);
        if (internal) {
            this.leftChannels.internalSet(Collections.unmodifiableSet(newKeys));
        } else {
            this.leftChannels.set(Collections.unmodifiableSet(newKeys));
        }
    }

    @Override
    public void joinChannel(final ChatChannel channel) {
        this.joinChannel(channel.key(), false);
    }

    public void leaveChannel(final ChatChannel channel, final boolean internal) {
        final Set<Key> newKeys = new HashSet<>(this.leftChannels.get());
        newKeys.add(channel.key());
        if (internal) {
            this.leftChannels.internalSet(Collections.unmodifiableSet(newKeys));
        } else {
            this.leftChannels.set(Collections.unmodifiableSet(newKeys));
        }
    }

    @Override
    public void leaveChannel(final ChatChannel channel) {
        this.leaveChannel(channel, false);
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
        if (!this.selectedChannel.hasValue()) {
            return this.channelRegistry.defaultChannel();
        }

        return this.channelRegistry.channel(this.selectedChannel.get());
    }

    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    public @Nullable Key selectedChannelKey() {
        return this.selectedChannel.orNull();
    }

    @Override
    public void selectedChannel(final @Nullable ChatChannel chatChannel) {
        if (chatChannel == null) {
            this.selectedChannel.set(null);
        } else {
            this.selectedChannel.set(chatChannel.key());
        }
    }

    @Override
    public ChannelMessage channelForMessage(final Component message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double distanceSquaredFrom(final CarbonPlayer other) {
        return -1;
    }

    @Override
    public boolean sameWorldAs(final CarbonPlayer other) {
        return false;
    }

    @Override
    public String username() {
        if (this.username == null) {
            this.username = Objects.requireNonNull(
                this.profileResolver.resolveName(this.uuid).join(),
                "name"
            );
        }

        return this.username;
    }

    public void username(final String username) {
        this.username = username;
    }

    public void markTransientLoaded(final boolean value) {
        if (value) {
            this.transientLoadedSince = System.currentTimeMillis();
        } else {
            this.transientLoadedSince = -1;
        }
    }

    public boolean transientLoadedNeedsUnload() {
        return this.transientLoadedSince != -1 && System.currentTimeMillis() - this.transientLoadedSince > KEEP_TRANSIENT_LOADS_FOR;
    }

    @Override
    public boolean hasCustomDisplayName() {
        if (!this.config.primaryConfig().useCarbonNicknames()) {
            return false;
        }
        return this.displayName.hasValue();
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        return this.uuid.equals(((CarbonPlayerCommon) other).uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    public void saved() {
        this.properties().forEach(PersistentUserProperty::saved);
    }

}
