package net.draycia.carbon.velocity.users;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerVelocity extends CarbonPlayerCommon {

    private final ProxyServer server;
    private final CarbonPlayer carbonPlayer;

    public CarbonPlayerVelocity(final ProxyServer server, final CarbonPlayer carbonPlayer) {
        this.server = server;
        this.carbonPlayer = carbonPlayer;
    }

    @Override
    public String username() {
        return this.carbonPlayer.username();
    }

    @Override
    public Component displayName() {
        return this.carbonPlayer.displayName();
    }

    @Override
    public UUID uuid() {
        return this.carbonPlayer.uuid();
    }

    @Override
    public Audience audience() {
        final @Nullable Player player = this.player();

        if (player == null) {
            return Audience.empty();
        }

        return player;
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
    public @Nullable ChatChannel selectedChannel() {
        return this.carbonPlayer.selectedChannel();
    }

    @Override
    public void selectedChannel(final ChatChannel chatChannel) {
        this.carbonPlayer.selectedChannel(chatChannel);
    }

    @Override
    public @NonNull Identity identity() {
        return this.carbonPlayer.identity();
    }

    @Override
    public CarbonPlayer carbonPlayer() {
        return this.carbonPlayer;
    }

    private @Nullable Player player() {
        return this.server.getPlayer(this.uuid).orElse(null);
    }

    @Override
    public @Nullable Locale locale() {
        final @Nullable Player player = this.player();

        if (player != null) {
            return player.getPlayerSettings().getLocale();
        } else {
            return null;
        }
    }

    @Override
    public Component createItemHoverComponent() {
        return Component.empty();
    }

    @Override
    public boolean hasPermission(final String permission) {
        final @Nullable Player player = this.player();

        if (player != null) {
            return player.hasPermission(permission);
        }

        return false;
    }

}
