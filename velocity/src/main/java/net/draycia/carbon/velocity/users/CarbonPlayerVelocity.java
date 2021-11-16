package net.draycia.carbon.velocity.users;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Locale;
import java.util.Optional;
import net.draycia.carbon.api.util.InventorySlot;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerVelocity extends WrappedCarbonPlayer implements ForwardingAudience.Single {

    private final ProxyServer server;
    private final CarbonPlayerCommon carbonPlayerCommon;

    public CarbonPlayerVelocity(final ProxyServer server, final CarbonPlayerCommon carbonPlayerCommon) {
        this.server = server;
        this.carbonPlayerCommon = carbonPlayerCommon;
    }

    @Override
    public @NotNull Audience audience() {
        return this.player().map(value -> (Audience) value).orElse(Audience.empty());
    }

    @Override
    public boolean vanished() {
        return false;
    }

    private Optional<Player> player() {
        return this.server.getPlayer(this.uuid());
    }

    @Override
    public CarbonPlayerCommon carbonPlayerCommon() {
        return this.carbonPlayerCommon;
    }

    @Override
    public @Nullable Locale locale() {
        return this.player().map(value -> value.getPlayerSettings().getLocale()).orElse(null);
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return null;
    }

    @Override
    public boolean online() {
        return this.player().isPresent();
    }

}
