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
package net.draycia.carbon.fabric;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.SaveOnChange;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class FabricUserManager implements UserManager<CarbonPlayerFabric>, SaveOnChange {

    protected final UserManager<CarbonPlayerCommon> proxiedUserManager;
    private final CarbonChatFabric carbonChatFabric;

    public FabricUserManager(final UserManager<CarbonPlayerCommon> proxiedUserManager, final CarbonChatFabric carbonChatFabric) {
        this.proxiedUserManager = proxiedUserManager;
        this.carbonChatFabric = carbonChatFabric;
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerFabric>> carbonPlayer(final UUID uuid) {
        return this.proxiedUserManager.carbonPlayer(uuid).thenApply(result -> {
            if (result.player() == null) {
                return new ComponentPlayerResult<>(null, result.reason());
            }

            return new ComponentPlayerResult<>(new CarbonPlayerFabric(result.player(), this.carbonChatFabric), result.reason());
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerFabric>> savePlayer(final CarbonPlayerFabric player) {
        return this.proxiedUserManager.savePlayer(player.carbonPlayerCommon()).thenApply(result -> {
            if (result.player() == null) {
                return new ComponentPlayerResult<>(null, result.reason());
            }

            return new ComponentPlayerResult<>(new CarbonPlayerFabric(result.player(), this.carbonChatFabric), result.reason());
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerFabric>> saveAndInvalidatePlayer(final CarbonPlayerFabric player) {
        return this.proxiedUserManager.saveAndInvalidatePlayer(player.carbonPlayerCommon()).thenApply(result -> {
            if (result.player() == null) {
                return new ComponentPlayerResult<>(null, result.reason());
            }

            return new ComponentPlayerResult<>(new CarbonPlayerFabric(result.player(), this.carbonChatFabric), result.reason());
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

    @Override
    public int addLeftChannel(final UUID id, final Key channel) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.addLeftChannel(id, channel);
        }

        return -1;
    }

    @Override
    public int removeLeftChannel(final UUID id, final Key channel) {
        if (this.proxiedUserManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange.removeLeftChannel(id, channel);
        }
        return -1;
    }

}
