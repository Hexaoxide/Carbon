package net.draycia.carbon.sponge.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static java.util.Objects.requireNonNull;

public record SpongePlayerCommander(
    @NonNull CarbonChat carbon,
    @NonNull ServerPlayer player,
    @NonNull CommandCause commandCause
) implements PlayerCommander, SpongeCommander {

    @Override
    public @NonNull CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.server().player(this.player.uniqueId()), "No CarbonPlayer for logged in Player!");
    }

    @Override
    public @NonNull Audience audience() {
        return this.commandCause.audience();
    }

}
