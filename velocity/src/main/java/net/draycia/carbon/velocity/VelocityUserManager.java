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
package net.draycia.carbon.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.SaveOnChange;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class VelocityUserManager implements UserManager<CarbonPlayerVelocity>, SaveOnChange {

    protected final UserManager<CarbonPlayerCommon> proxiedUserManager;
    private final ProxyServer proxyServer;

    public VelocityUserManager(final UserManager<CarbonPlayerCommon> proxiedUserManager, final ProxyServer proxyServer) {
        this.proxiedUserManager = proxiedUserManager;
        this.proxyServer = proxyServer;
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerVelocity>> carbonPlayer(final UUID uuid) {
        return this.proxiedUserManager.carbonPlayer(uuid).thenApply(result -> {
            if (result.player() == null) {
                return new ComponentPlayerResult<>(null, result.reason());
            }

            return new ComponentPlayerResult<>(new CarbonPlayerVelocity(this.proxyServer, result.player()), result.reason());
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerVelocity>> savePlayer(final CarbonPlayerVelocity player) {
        return this.proxiedUserManager.savePlayer(player.carbonPlayerCommon()).thenApply(result -> {
            if (result.player() == null) {
                return new ComponentPlayerResult<>(null, result.reason());
            }

            return new ComponentPlayerResult<>(new CarbonPlayerVelocity(this.proxyServer, result.player()), result.reason());
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerVelocity>> saveAndInvalidatePlayer(final CarbonPlayerVelocity player) {
        return this.proxiedUserManager.saveAndInvalidatePlayer(player.carbonPlayerCommon()).thenApply(result -> {
            if (result.player() == null) {
                return new ComponentPlayerResult<>(null, result.reason());
            }

            return new ComponentPlayerResult<>(new CarbonPlayerVelocity(this.proxyServer, result.player()), result.reason());
        });
    }

    @Override
    public int saveDisplayName(final UUID id, final @Nullable Component component) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveDisplayName(id, component);
        }

        return -1;
    }

    @Override
    public int saveMuted(final UUID id, final boolean muted) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveMuted(id, muted);
        }

        return -1;
    }

    @Override
    public int saveDeafened(final UUID id, final boolean deafened) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveDeafened(id, deafened);
        }

        return -1;
    }

    @Override
    public int saveSpying(final UUID id, final boolean spying) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveSpying(id, spying);
        }

        return -1;
    }

    @Override
    public int saveSelectedChannel(final UUID id, final @Nullable Key selectedChannel) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveSelectedChannel(id, selectedChannel);
        }

        return -1;
    }

    @Override
    public int saveLastWhisperTarget(final UUID id, final @Nullable UUID lastWhisperTarget) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveLastWhisperTarget(id, lastWhisperTarget);
        }

        return -1;
    }

    @Override
    public int saveWhisperReplyTarget(final UUID id, final @Nullable UUID whisperReplyTarget) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.saveWhisperReplyTarget(id, whisperReplyTarget);
        }

        return -1;
    }

    @Override
    public int addIgnore(final UUID id, final UUID ignoredPlayer) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.addIgnore(id, ignoredPlayer);
        }

        return -1;
    }

    @Override
    public int removeIgnore(final UUID id, final UUID ignoredPlayer) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.removeIgnore(id, ignoredPlayer);
        }

        return -1;
    }

}
