package net.draycia.carbon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public record VelocityPlayerCommander(
    CarbonChat carbon,
    Player player
) implements PlayerCommander, VelocityCommander {

    @Override
    public CommandSource commandSource() {
        return this.player;
    }

    @Override
    public Audience audience() {
        return this.player;
    }

    @Override
    public CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.server().player(this.player.getUniqueId()).join(), "No CarbonPlayer for logged in Player!");
    }

}
