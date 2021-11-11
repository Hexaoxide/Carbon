package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.common.config.PrimaryConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

@DefaultQualifier(NonNull.class)
public class SpongePlayerJoinListener {

    private final CarbonChat carbonChat;
    private final PrimaryConfig primaryConfig;

    @Inject
    public SpongePlayerJoinListener(
        final CarbonChat carbonChat,
        final PrimaryConfig primaryConfig
    ) {
        this.carbonChat = carbonChat;
        this.primaryConfig = primaryConfig;
    }

    @Listener
    public void onPlayerLogin(final ServerSideConnectionEvent.Join event) {
        final ComponentPlayerResult<CarbonPlayer> result = this.carbonChat.server().player(event.player().uniqueId()).join();
        final @Nullable CarbonPlayer player = result.player();

        if (player == null) {
            return;
        }

        // Don't show join messages when muted
        if (this.primaryConfig.hideMutedJoinLeaveQuit() && !player.muteEntries().isEmpty()) {
            event.setMessageCancelled(true);
        }
    }

    @Listener
    public void onPlayerQuit(final ServerSideConnectionEvent.Disconnect event) {
        // Early exit in case "hide muted join / leave messages when muted" is disabled
        if (!this.primaryConfig.hideMutedJoinLeaveQuit()) {
            return;
        }

        final ComponentPlayerResult<CarbonPlayer> result = this.carbonChat.server().player(event.player().uniqueId()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer player = result.player();

        // Don't show quit messages when muted
        if (!player.muteEntries().isEmpty()) {
            event.setMessage(null); // TODO: null? empty? it's not MessageCancellable, which is odd
        }
    }

}
