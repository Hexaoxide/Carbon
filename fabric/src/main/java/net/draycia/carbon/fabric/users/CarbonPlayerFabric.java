package net.draycia.carbon.fabric.users;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.platform.fabric.PlayerLocales;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@DefaultQualifier(NonNull.class)
public class CarbonPlayerFabric extends WrappedCarbonPlayer implements ForwardingAudience.Single {

    private final CarbonPlayerCommon carbonPlayerCommon;
    private final CarbonChatFabric carbonChatFabric;

    public CarbonPlayerFabric(CarbonPlayerCommon carbonPlayerCommon, CarbonChatFabric carbonChatFabric) {
        this.carbonPlayerCommon = carbonPlayerCommon;
        this.carbonChatFabric = carbonChatFabric;
    }

    @Override
    public @NotNull Audience audience() {
        final ServerPlayer player = this.player();
        return FabricServerAudiences.of(player.server).audience(player);
    }

    private ServerPlayer player() {
        return this.carbonChatFabric.minecraftServer().getPlayerList().getPlayer(this.carbonPlayerCommon.uuid());
    }

    @Override
    public boolean vanished() {
        return false;
    }

    @Override
    public boolean awareOf(CarbonPlayer other) {
        return false;
    }

    @Override
    public @Nullable Locale locale() {
        return PlayerLocales.locale(player());
    }

    @Override
    public boolean online() {
        return this.carbonChatFabric.minecraftServer().getPlayerList().getPlayer(player().getUUID()) != null;
    }

    @Override
    public CarbonPlayerCommon carbonPlayerCommon() {
        return this.carbonPlayerCommon;
    }
}
