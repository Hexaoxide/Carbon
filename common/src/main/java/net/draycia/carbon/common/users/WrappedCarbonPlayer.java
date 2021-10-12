package net.draycia.carbon.common.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.punishments.MuteEntry;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@DefaultQualifier(NonNull.class)
public abstract class WrappedCarbonPlayer implements CarbonPlayer {

    public abstract CarbonPlayerCommon carbonPlayerCommon();

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
    public void displayName(@Nullable Component displayName) {
        this.carbonPlayerCommon().displayName(displayName);
    }

    @Override
    public void temporaryDisplayName(@Nullable Component temporaryDisplayName, long expirationEpoch) {
        this.carbonPlayerCommon().temporaryDisplayName(temporaryDisplayName, expirationEpoch);
    }

    @Override
    public @Nullable Component temporaryDisplayName() {
        return this.carbonPlayerCommon().temporaryDisplayName();
    }

    @Override
    public long temporaryDisplayNameExpiration() {
        return this.carbonPlayerCommon().temporaryDisplayNameExpiration();
    }

    @Override
    public UUID uuid() {
        return this.carbonPlayerCommon().uuid();
    }

    @Override
    public Component createItemHoverComponent() {
        return this.carbonPlayerCommon().createItemHoverComponent();
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
    public void selectedChannel(ChatChannel chatChannel) {
        this.carbonPlayerCommon().selectedChannel(chatChannel);
    }

    // TODO: move to Permissible class or one similarly named?
    @Override
    public boolean hasPermission(String permission) {
        return this.carbonPlayerCommon().hasPermission(permission);
    }

    @Override
    public String primaryGroup() {
        return this.carbonPlayerCommon().primaryGroup();
    }

    @Override
    public List<String> groups() {
        return this.carbonPlayerCommon().groups();
    }

    @Override
    public List<MuteEntry> muteEntries() {
        return this.carbonPlayerCommon().muteEntries();
    }

    @Override
    public boolean muted(final ChatChannel chatChannel) {
        return this.carbonPlayerCommon().muted(chatChannel);
    }

    @Override
    public @Nullable MuteEntry addMuteEntry(
        final @Nullable ChatChannel chatChannel,
        final boolean muted,
        final @Nullable UUID cause,
        final long duration,
        final @Nullable String reason
    ) {
        return this.carbonPlayerCommon().addMuteEntry(chatChannel, muted, cause, duration, reason);
    }

    @Override
    public boolean deafened() {
        return this.carbonPlayerCommon().deafened();
    }

    @Override
    public void deafened(boolean deafened) {
        this.carbonPlayerCommon().deafened(deafened);
    }

    @Override
    public boolean spying() {
        return this.carbonPlayerCommon().spying();
    }

    @Override
    public void spying(boolean spying) {
        this.carbonPlayerCommon().spying(spying);
    }

    @Override
    public void sendMessageAsPlayer(String message) {
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
    public void whisperReplyTarget(@Nullable UUID uuid) {
        this.carbonPlayerCommon().whisperReplyTarget(uuid);
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.carbonPlayerCommon().lastWhisperTarget();
    }

    @Override
    public void lastWhisperTarget(@Nullable UUID uuid) {
        this.carbonPlayerCommon().lastWhisperTarget(uuid);
    }

    @Override
    public @NotNull Identity identity() {
        return this.carbonPlayerCommon().identity();
    }

}
