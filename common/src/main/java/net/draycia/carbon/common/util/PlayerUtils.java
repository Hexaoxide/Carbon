package net.draycia.carbon.common.util;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;

public class PlayerUtils {

    private PlayerUtils() {

    }

    public static void savePlayers(final CarbonServer carbonServer, final UserManager<CarbonPlayerCommon> userManager) {
        for (final var player : carbonServer.players()) {
            final var saveResult = userManager.savePlayer(((WrappedCarbonPlayer) player).carbonPlayerCommon());

            saveResult.thenAccept(result -> {
                if (result.player() == null) {
                    carbonServer.console().sendMessage(result.reason());
                }
            });

            saveResult.exceptionally(exception -> {
                exception.getCause().printStackTrace();
                return null;
            });
        }

    }
}
