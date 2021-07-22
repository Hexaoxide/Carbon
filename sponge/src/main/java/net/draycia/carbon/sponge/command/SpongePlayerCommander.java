package net.draycia.carbon.sponge.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public record SpongePlayerCommander(
    CarbonChat carbon,
    ServerPlayer player,
    CommandCause commandCause
) implements PlayerCommander, SpongeCommander {

    @Override
    public CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.server().player(this.player.uniqueId()).join().player(), "No CarbonPlayer for logged in Player!");
    }

    @Override
    public @NotNull Audience audience() {
        return this.commandCause.audience();
    }

}
