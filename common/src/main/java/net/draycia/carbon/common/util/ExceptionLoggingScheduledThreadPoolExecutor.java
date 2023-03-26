/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ExceptionLoggingScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    private final Logger logger;

    public ExceptionLoggingScheduledThreadPoolExecutor(final int corePoolSize, final ThreadFactory threadFactory, final Logger logger) {
        super(corePoolSize, threadFactory);
        this.logger = logger;
    }

    @Override
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return super.schedule(new ExceptionLoggingRunnable(command, this.logger), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        return super.scheduleAtFixedRate(new ExceptionLoggingRunnable(command, this.logger), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        return super.scheduleWithFixedDelay(new ExceptionLoggingRunnable(command, this.logger), initialDelay, delay, unit);
    }

    private record ExceptionLoggingRunnable(Runnable wrapped, Logger logger) implements Runnable {
        @Override
        public void run() {
            try {
                this.wrapped.run();
            } catch (final Throwable thr) {
                this.logger.error("Error executing task '{}'", this.wrapped, thr);
                Exceptions.rethrow(thr);
            }
        }
    }

}
