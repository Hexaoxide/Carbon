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
package net.draycia.carbon.paper.integration.plotsquared;

import com.google.inject.Inject;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.channels.ChannelPermissions;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.config.ConfigHeader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

@NullMarked
@ConfigSerializable
@ConfigHeader(PlotChannel.PLOT_CHANNEL_HEADER)
public class PlotChannel extends ConfigChatChannel {

    protected static final String PLOT_CHANNEL_HEADER = """
            See the PlotSquared Wiki at https://intellectualsites.gitbook.io/plotsquared/customization/placeholders
            for placeholders PlotSquared provides to PlaceholderAPI.
            """;
    protected final static PlotAPI PLOT_API = new PlotAPI();

    public static final String FILE_NAME = "plotsquared-plotchat.conf";

    protected transient @MonotonicNonNull @Inject UserManager<?> users;

    public PlotChannel() {
        this.key = Key.key("carbon", "plotchat");
        this.commandAliases = List.of("local");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
                "default_format", "<display_name>: <message>",
                "console", "<username>: <message>"
        );
    }

    protected @Nullable Plot plot(final CarbonPlayer carbonPlayer) {
        final @Nullable PlotPlayer<?> plotPlayer = PLOT_API.wrapPlayer(carbonPlayer.uuid());
        if (plotPlayer != null) {
            return plotPlayer.getCurrentPlot();
        }
        return null;
    }

    @Override
    public ChannelPermissions permissions() {
        return ChannelPermissions.uniformDynamic(player -> channelPermissionResult(
                this.plot(player) != null,
                () -> this.cannotUseChannel(player)
        ));
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final @Nullable Plot plot = this.plot(sender);

        if (plot == null) {
            if (sender.online()) {
                sender.sendMessage(this.cannotUseChannel(sender));
            }

            return Collections.emptyList();
        }

        final List<Audience> recipients = new ArrayList<>();
        for (final PlotPlayer<?> plotPlayer : this.onlinePlayers(plot)) {
            final @Nullable CarbonPlayer carbon = this.users.user(plotPlayer.getUUID()).getNow(null);
            if (carbon != null) {
                recipients.add(carbon);
            }
        }

        recipients.add(this.server.console());

        return recipients;
    }

    protected List<PlotPlayer<?>> onlinePlayers(final Plot plot) {
        return plot.getPlayersInPlot();
    }

    protected Component cannotUseChannel(final CarbonPlayer player) {
        return this.messages.cannotUsePlotChannel(player);
    }

    @Override
    public boolean shouldCrossServer() {
        return false;
    }

}
