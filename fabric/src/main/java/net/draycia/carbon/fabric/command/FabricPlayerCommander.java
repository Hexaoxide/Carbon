package net.draycia.carbon.fabric.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import static java.util.Objects.requireNonNull;

public record FabricPlayerCommander(
    @NonNull MinecraftServer server,
    @NonNull CarbonChat carbon,
    @NonNull CommandSource commandSource
) implements PlayerCommander, FabricCommander {

    @Override
    public @NonNull CommandSource commandSource() {
        return this.commandSource;
    }

    @Override
    public @NonNull Audience audience() {
        return FabricServerAudiences.of(this.server).audience(this.commandSource);
    }

    @Override
    public @NonNull CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.server().player(((Player) this.commandSource).getUUID()).join().player(), "No CarbonPlayer for logged in Player!");
    }

}
