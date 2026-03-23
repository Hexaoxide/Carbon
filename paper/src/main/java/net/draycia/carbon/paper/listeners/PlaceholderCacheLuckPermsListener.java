/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.paper.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.atomic.AtomicBoolean;
import net.draycia.carbon.paper.messages.PlaceholderValueCache;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class PlaceholderCacheLuckPermsListener {

    private final PlaceholderValueCache placeholderValueCache;
    private final AtomicBoolean registered = new AtomicBoolean(false);

    @Inject
    private PlaceholderCacheLuckPermsListener(final PlaceholderValueCache placeholderValueCache) {
        this.placeholderValueCache = placeholderValueCache;
    }

    public boolean register() {
        if (this.registered.get()) {
            return true;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            return false;
        }

        try {
            // compareAndSet ensures only one thread ever subscribes, even if register() is
            // called concurrently from the startup path and a retry scheduler.
            if (!this.registered.compareAndSet(false, true)) {
                return true;
            }
            LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
                this.placeholderValueCache.invalidate(event.getUser().getUniqueId());
            });
            return true;
        } catch (final IllegalStateException ignored) {
            // LuckPerms is enabled but not ready yet; reset so we can retry.
            this.registered.set(false);
            return false;
        }
    }

}
