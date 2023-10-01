package net.draycia.carbon.sponge;

import com.google.inject.Singleton;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.PlatformScheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.scheduler.Task;

@Singleton
@DefaultQualifier(NonNull.class)
public final class SpongeScheduler implements PlatformScheduler {

    @Override
    public void scheduleForPlayer(final CarbonPlayer carbonPlayer, final Runnable runnable) {
        Task.builder().execute(runnable);
    }

}
