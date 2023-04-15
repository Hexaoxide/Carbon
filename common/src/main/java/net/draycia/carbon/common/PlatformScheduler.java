package net.draycia.carbon.common;

import net.draycia.carbon.api.users.CarbonPlayer;

public interface PlatformScheduler {

    void scheduleForPlayer(final CarbonPlayer carbonPlayer, final Runnable runnable);

}
