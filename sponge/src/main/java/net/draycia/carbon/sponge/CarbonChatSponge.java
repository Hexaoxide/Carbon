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
import com.google.inject.TypeLiteral;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ListenerUtils;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import net.draycia.carbon.sponge.listeners.SpongePlayerJoinListener;
import net.draycia.carbon.sponge.listeners.SpongeReloadListener;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSponge implements CarbonChat {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(SpongeChatListener.class,
        SpongePlayerJoinListener.class, SpongeReloadListener.class);

    private static final int BSTATS_PLUGIN_ID = 11279;

    private final CarbonMessageService messageService;
    private final CarbonServerSponge carbonServerSponge;
    private final ChannelRegistry channelRegistry;
    private final Injector injector;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private final UserManager<CarbonPlayerCommon> userManager;
    private final CarbonEventHandler eventHandler = new CarbonEventHandler();

    @Inject
    public CarbonChatSponge(
        //final Metrics.Factory metricsFactory,
        final Game game,
        final PluginContainer pluginContainer,
        final Injector injector,
        final Logger logger,
        @ConfigDir(sharedRoot = false) final Path dataDirectory
    ) {
        CarbonChatProvider.register(this);

        this.pluginContainer = pluginContainer;

        this.injector = injector.createChildInjector(new CarbonChatSpongeModule(this, dataDirectory, pluginContainer));
        this.logger = logger;
        this.messageService = this.injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServerSponge = this.injector.getInstance(CarbonServerSponge.class);
        this.userManager = this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<UserManager<CarbonPlayerCommon>>() {}));
        this.dataDirectory = dataDirectory;

        for (final Class<?> clazz : LISTENER_CLASSES) {
            game.eventManager().registerListeners(this.pluginContainer, this.injector.getInstance(clazz));
        }

        //metricsFactory.make(BSTATS_PLUGIN_ID);

        // Listeners
        ListenerUtils.registerCommonListeners(this.injector);

        // Load channels
        ((CarbonChannelRegistry) this.channelRegistry()).loadConfigChannels();

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

        // Commands
        CloudUtils.registerCommands(this.injector);
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        // Player data saving
        Sponge.asyncScheduler().submit(Task.builder()
            .interval(5, TimeUnit.MINUTES)
            .plugin(this.pluginContainer)
            .execute(() -> PlayerUtils.saveLoggedInPlayers(this.carbonServerSponge, this.userManager))
            .build());
    }

    @Listener
    public void onDisable(final StoppingEngineEvent<Server> event) {
        PlayerUtils.saveLoggedInPlayers(this.carbonServerSponge, this.userManager).forEach(CompletableFuture::join);
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public CarbonServerSponge server() {
        return this.carbonServerSponge;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    @Override
    public <T extends Audience> IMessageRenderer<T, String, RenderedMessage, Component> messageRenderer() {
        return this.injector.getInstance(SpongeMessageRenderer.class);
    }

    public CarbonMessageService messageService() {
        return this.messageService;
    }

    @Override
    public @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

}
