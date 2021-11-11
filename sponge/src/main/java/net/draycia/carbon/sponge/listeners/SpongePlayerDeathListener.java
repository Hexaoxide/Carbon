package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.common.config.PrimaryConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

@DefaultQualifier(NonNull.class)
public class SpongePlayerDeathListener {

    private final CarbonChat carbonChat;
    private final PrimaryConfig primaryConfig;

    @Inject
    public SpongePlayerDeathListener(
        final CarbonChat carbonChat,
        final PrimaryConfig primaryConfig
    ) {
        this.carbonChat = carbonChat;
        this.primaryConfig = primaryConfig;
    }

    @Listener
    public void onPlayerDeath(final DestructEntityEvent.Death event, final @First Player player) {
        // Early exit in case "hide muted join / leave messages when muted" is disabled
        if (!this.primaryConfig.hideMutedJoinLeaveQuit()) {
            return;
        }

        final ComponentPlayerResult<CarbonPlayer> result = this.carbonChat.server().player(player.uniqueId()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer carbonPlayer = result.player();

        if (!carbonPlayer.muteEntries().isEmpty()) {
            event.setMessageCancelled(true);
        }
    }

}
