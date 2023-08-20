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
package net.draycia.carbon.common.users.db;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.CachingUserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

@DefaultQualifier(NonNull.class)
public abstract class DatabaseUserManager extends CachingUserManager {

    protected final Jdbi jdbi;
    protected final QueriesLocator locator;
    protected final ChannelRegistry channelRegistry;

    protected DatabaseUserManager(
        final Jdbi jdbi,
        final QueriesLocator locator,
        final Logger logger,
        final ProfileResolver profileResolver,
        final MembersInjector<CarbonPlayerCommon> playerInjector,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory,
        final ChannelRegistry channelRegistry
    ) {
        super(
            logger,
            profileResolver,
            playerInjector,
            messagingManager,
            packetFactory
        );
        this.jdbi = jdbi;
        this.locator = locator;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public final void saveSync(final CarbonPlayerCommon player) {
        this.jdbi.useTransaction(handle -> {
            handle.createUpdate(this.locator.query("insert-player"))
                .bind("id", player.uuid())
                .bind("muted", player.muted())
                .bind("deafened", player.deafened())
                .bind("selectedchannel", player.selectedChannelKey())
                .bind("displayname", player.displayNameRaw())
                .bind("lastwhispertarget", player.lastWhisperTarget())
                .bind("whisperreplytarget", player.whisperReplyTarget())
                .bind("spying", player.spying())
                .execute();

            handle.createUpdate(this.locator.query("clear-ignores"))
                .bind("id", player.uuid())
                .execute();
            handle.createUpdate(this.locator.query("clear-leftchannels"))
                .bind("id", player.uuid())
                .execute();

            final Set<UUID> ignored = player.ignoredPlayers();
            if (!ignored.isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-ignores"));
                for (final UUID ignoredPlayer : ignored) {
                    batch.bind("id", player.uuid()).bind("ignoredplayer", ignoredPlayer).add();
                }
                batch.execute();
            }

            final List<Key> left = player.leftChannels();
            if (!left.isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-leftchannels"));
                for (final Key leftChannel : left) {
                    batch.bind("id", player.uuid()).bind("channel", leftChannel).add();
                }
                batch.execute();
            }

            handle.commit();
        });
    }

}
