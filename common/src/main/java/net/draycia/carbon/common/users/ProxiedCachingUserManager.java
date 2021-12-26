/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.users;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class ProxiedCachingUserManager implements UserManager<CarbonPlayerCommon> {

    private final Map<UUID, CarbonPlayerCommon> userCache = new ConcurrentHashMap<>();
    private final UserManager<CarbonPlayerCommon> proxiedUserManager;
    private final CarbonChat carbonChat;

    public ProxiedCachingUserManager(final UserManager<CarbonPlayerCommon> proxiedUserManager, final CarbonChat carbonChat) {
        this.proxiedUserManager = proxiedUserManager;
        this.carbonChat = carbonChat;
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> carbonPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayerCommon cachedPlayer = this.userCache.get(uuid);

            if (cachedPlayer != null) {
                return new ComponentPlayerResult<>(cachedPlayer, Component.empty());
            }

            final ComponentPlayerResult<CarbonPlayerCommon> result = this.proxiedUserManager.carbonPlayer(uuid).join();

            if (result.player() != null) {
                this.userCache.put(uuid, result.player());

                return new ComponentPlayerResult<>(result.player(), Component.empty());
            }

            final @Nullable String name = this.carbonChat.server().resolveName(uuid).join();

            if (name != null) {
                final CarbonPlayerCommon player = new CarbonPlayerCommon(name, uuid);

                this.userCache.put(uuid, player);

                return new ComponentPlayerResult<>(player, Component.empty());
            }

            return new ComponentPlayerResult<>(null, text("Name not found for uuid!"));
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> savePlayer(final CarbonPlayerCommon player) {
        return this.proxiedUserManager.savePlayer(player);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> saveAndInvalidatePlayer(final CarbonPlayerCommon player) {
        return this.proxiedUserManager.saveAndInvalidatePlayer(player);
    }

}
