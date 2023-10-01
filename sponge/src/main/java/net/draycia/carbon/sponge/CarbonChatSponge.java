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
package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.common.CarbonChatInternal;
import net.draycia.carbon.common.PeriodicTasks;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import net.draycia.carbon.sponge.listeners.SpongePlayerJoinListener;
import net.draycia.carbon.sponge.listeners.SpongeReloadListener;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSponge extends CarbonChatInternal {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(SpongeChatListener.class,
        SpongePlayerJoinListener.class, SpongeReloadListener.class);

    private static final int BSTATS_PLUGIN_ID = 11279;

    private final PluginContainer pluginContainer;

    @Inject
    public CarbonChatSponge(
        //final Metrics.Factory metricsFactory,
        final PluginContainer pluginContainer,
        final Injector injector,
        final Logger logger,
        @PeriodicTasks final ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final PlatformUserManager userManager,
        final ExecutionCoordinatorHolder commandExecutor,
        final CarbonServer carbonServer,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler eventHandler,
        final CarbonChannelRegistry channelRegistry,
        final Provider<MessagingManager> messagingManager
    ) {
        super(
            injector,
            logger,
            periodicTasks,
            profileCache,
            profileResolver,
            userManager,
            commandExecutor,
            carbonServer,
            carbonMessages,
            eventHandler,
            channelRegistry,
            messagingManager
        );
        this.pluginContainer = pluginContainer;
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        this.init();

        for (final Class<?> clazz : LISTENER_CLASSES) {
            event.game().eventManager().registerListeners(this.pluginContainer, this.injector().getInstance(clazz));
        }

        // TODO: metrics
        //metricsFactory.make(BSTATS_PLUGIN_ID);

        this.checkVersion();

        // TODO: Register these in a central location, pull from that in this and plugin.yml
        Sponge.serviceProvider().provide(PermissionService.class).ifPresent(permissionService -> {
            final PermissionDescription.Builder builder = permissionService.newDescriptionBuilder(this.pluginContainer);

            builder.id("carbon.clearchat.clear")
                .description(Component.text("Clears the chat for all players except those with carbon.chearchat.exempt."))
                .register();

            builder.id("carbon.clearchat.exempt")
                .description(Component.text("Exempts the player from having their chat cleared when /clearchat is executed."))
                .register();

            builder.id("carbon.debug")
                .description(Component.text("Allows the sender to quickly check what carbon think's the player's primary and non-primary groups are."))
                .register();

            builder.id("carbon.help")
                .description(Component.text("Shows Carbon's help menu, detailing each part of Carbon's commands."))
                .register();

            builder.id("carbon.hideidentity")
                .description(Component.text("Prevents messages from the player from being blocked clientside."))
                .register();

            builder.id("carbon.ignore")
                .description(Component.text("Ignores the player, hiding messages they send in chat and in whispers."))
                .register();

            builder.id("carbon.ignore.exempt")
                .description(Component.text("Prevents the player from being ignored."))
                .register();

            builder.id("carbon.ignore.unignore")
                .description(Component.text("Removes the player from the sender's ignore list."))
                .register();

            builder.id("carbon.itemlink")
                .description(Component.text("Shows the player's held or equipped item in chat."))
                .register();

            builder.id("carbon.mute")
                .description(Component.text("Mutes the player, preventing them from sending messages or whispers."))
                .register();

            builder.id("carbon.mute.exempt")
                .description(Component.text("Prevents the player from being muted."))
                .register();

            builder.id("carbon.mute.info")
                .description(Component.text("Shows if the player is muted or now."))
                .register();

            builder.id("carbon.mute.notify")
                .description(Component.text("Notifies the player when someone else has been mute."))
                .register();

            builder.id("carbon.mute.unmute")
                .description(Component.text("Unmutes the player, allowing them to use chat and send whispers."))
                .register();

            builder.id("carbon.nickname")
                .description(Component.text("The nickname command, by default shows your nickname."))
                .register();

            builder.id("carbon.nickname.others")
                .description(Component.text("Checks/sets other player's nicknames."))
                .register();

            builder.id("carbon.nickname.see")
                .description(Component.text("Checks your/other player's nicknames."))
                .register();

            builder.id("carbon.nickname.self")
                .description(Component.text("Checks/sets your nickname."))
                .register();

            builder.id("carbon.nickname.set")
                .description(Component.text("Sets your/other player's nicknames."))
                .register();

            builder.id("carbon.reload")
                .description(Component.text("Reloads Carbon's config, channel settings, and translations."))
                .register();

            builder.id("carbon.whisper")
                .description(Component.text("Sends private messages to other players."))
                .register();

            builder.id("carbon.whisper.continue")
                .description(Component.text("Sends a message to the last player you whispered."))
                .register();

            builder.id("carbon.whisper.reply")
                .description(Component.text("Sends a message to the last player who messaged you."))
                .register();

            builder.id("carbon.whisper.vanished")
                .description(Component.text("Allows the player to send messages to vanished players."))
                .register();
        });
    }

    @Listener
    public void onDisable(final StoppingEngineEvent<Server> event) {
        this.shutdown();
    }

}
