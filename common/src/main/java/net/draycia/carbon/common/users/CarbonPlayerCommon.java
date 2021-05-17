package net.draycia.carbon.common.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.VanillaChatChannel;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public abstract class CarbonPlayerCommon implements CarbonPlayer, ForwardingAudience.Single {

    protected final String username;
    protected Component displayName;
    protected final UUID uuid;
    protected final Identity identity;
    protected ChatChannel selectedChannel;

    protected CarbonPlayerCommon(
        final String username,
        final Component displayName,
        final UUID uuid,
        final Identity identity
    ) {
        this.username = username;
        this.displayName = displayName;
        this.uuid = uuid;
        this.identity = identity;
        this.selectedChannel = new VanillaChatChannel(); // TODO: replace, persist and use global instance
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public Component displayName() {
        return this.displayName;
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.displayName = requireNonNullElseGet(displayName, () -> text(this.username));
    }

    @Override
    public Identity identity() {
        return this.identity;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public ChatChannel selectedChannel() {
        return this.selectedChannel;
    }

    @Override
    public void selectedChannel(final ChatChannel chatChannel) {
        this.selectedChannel = chatChannel;
    }

}
