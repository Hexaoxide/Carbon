package net.draycia.carbon.common.users;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class CarbonPlayerCommon implements CarbonPlayer, ForwardingAudience.Single {

    protected boolean deafened = false;
    protected @Nullable Component displayName;
    protected boolean muted = false;
    protected @Nullable ChatChannel selectedChannel;
    protected boolean spying = false;
    protected String username;
    protected UUID uuid;

    public CarbonPlayerCommon(
        final @Nullable Component displayName,
        final @Nullable ChatChannel selectedChannel,
        final String username,
        final UUID uuid
    ) {
        this.displayName = displayName;
        this.selectedChannel = selectedChannel;
        this.username = username;
        this.uuid = uuid;
    }

    public CarbonPlayerCommon() {

    }

    @Override
    public @NonNull Audience audience() {
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
    public Component displayName() {
        return requireNonNullElseGet(this.displayName, () -> text(this.username));
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.displayName = requireNonNullElseGet(displayName, () -> text(this.username));
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
    public boolean muted() {
        return this.muted;
    }

    @Override
    public void muted(final boolean muted) {
        this.muted = muted;
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
    public void selectedChannel(final ChatChannel chatChannel) {
        this.selectedChannel = chatChannel;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

}
