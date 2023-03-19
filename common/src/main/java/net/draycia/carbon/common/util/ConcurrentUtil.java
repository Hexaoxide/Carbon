package net.draycia.carbon.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ConcurrentUtil {

    private ConcurrentUtil() {
    }

    public static void shutdownExecutor(final ExecutorService service, final TimeUnit timeoutUnit, final long timeoutLength) {
        service.shutdown();
        boolean didShutdown;
        try {
            didShutdown = service.awaitTermination(timeoutLength, timeoutUnit);
        } catch (final InterruptedException ignore) {
            didShutdown = false;
        }
        if (!didShutdown) {
            service.shutdownNow();
        }
    }

    public static ThreadFactory carbonThreadFactory(final Logger logger, final String name) {
        return new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("CarbonChat " + name + " Thread #%d")
            .setUncaughtExceptionHandler((thread, thr) -> logger.warn("Uncaught exception on thread {}", thread.getName(), thr))
            .build();
    }

}
