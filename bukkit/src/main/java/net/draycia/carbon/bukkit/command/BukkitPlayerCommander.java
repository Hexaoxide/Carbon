package net.draycia.carbon.bukkit.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

public record BukkitPlayerCommander(
    @NonNull CarbonChat carbon,
    @NonNull Player player
) implements PlayerCommander, BukkitCommander {

    @Override
    public @NonNull CommandSender commandSender() {
        return this.player;
    }

    @Override
    public @NonNull Audience audience() {
        return this.player;
    }

    @Override
    public @NonNull CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.userManager().carbonPlayer(this.player.getUniqueId()), "No CarbonPlayer for logged in Player!");
    }

}
