package net.draycia.carbon.common.users;

import java.util.*;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.punishments.MuteEntry;
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
    protected @Nullable UUID lastWhisperTarget = null;
    protected @Nullable UUID whisperReplyTarget = null;

    // Administrative
    protected boolean spying = false;

    // Punishments
    protected List<MuteEntry> muteEntries = new ArrayList<>();

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
    public Component createItemHoverComponent() {
        return Component.empty();
    }

    @Override
    public @Nullable Component displayName() {
        if (this.temporaryDisplayName != null && System.currentTimeMillis() < this.temporaryDisplayNameExpiration) {
            return this.temporaryDisplayName;
        }

        if (this.displayName != null) {
            return this.displayName;
        } else

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
    public void sendMessageAsPlayer(String message) { }

    @Override
    public boolean online() {
        return false;
    }

    @Override
    public @Nullable UUID whisperReplyTarget() {
        return this.whisperReplyTarget;
    }

    @Override
    public void whisperReplyTarget(@Nullable UUID uuid) {
        this.whisperReplyTarget = uuid;
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.lastWhisperTarget;
    }

    @Override
    public void lastWhisperTarget(@Nullable UUID uuid) {
        this.lastWhisperTarget = uuid;
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
