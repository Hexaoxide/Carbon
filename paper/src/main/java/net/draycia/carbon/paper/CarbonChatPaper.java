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
package net.draycia.carbon.paper;

import com.google.inject.Inject;
import com.google.inject.Injector;
import github.scarsz.discordsrv.DiscordSRV;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.commands.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.listeners.RadiusListener;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ListenerUtils;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.paper.hooks.DSRVChatHook;
import net.draycia.carbon.paper.listeners.DiscordMessageListener;
import net.draycia.carbon.paper.listeners.PaperChatListener;
import net.draycia.carbon.paper.listeners.PaperPlayerJoinListener;
import net.draycia.carbon.paper.messages.PaperMessageRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import ninja.egg82.messenger.services.PacketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatPaper implements CarbonChat {

    private static final Set<Class<? extends Listener>> LISTENER_CLASSES = Set.of(
        PaperChatListener.class,
        PaperPlayerJoinListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 8720;
    private final CarbonEventHandler eventHandler = new CarbonEventHandler();
    private @MonotonicNonNull JavaPlugin plugin;
    private @MonotonicNonNull Logger logger;
    private @MonotonicNonNull Path dataDirectory;
    private @MonotonicNonNull Injector injector;
    private @MonotonicNonNull UserManagerInternal<CarbonPlayerCommon> userManager;
    private @MonotonicNonNull CarbonServer carbonServer;
    private @MonotonicNonNull CarbonMessages carbonMessages;
    private @MonotonicNonNull ChannelRegistry channelRegistry;
    private final UUID serverId = UUID.randomUUID();

    private @MonotonicNonNull MessagingManager messagingManager = null;

    CarbonChatPaper() {
    }

    @Inject
    private void onLoad(
        final Injector injector,
        final JavaPlugin plugin,
        final CarbonMessages carbonMessages,
        final ChannelRegistry channelRegistry,
        final CarbonServer carbonServer,
        final UserManager<CarbonPlayerCommon> userManager,
        @ForCarbon final Path dataDirectory
    ) {
        this.logger = LogManager.getLogger("CarbonChat");
        this.injector = injector;
        this.plugin = plugin;
        this.carbonMessages = carbonMessages;
        this.channelRegistry = channelRegistry;
        this.carbonServer = carbonServer;
        this.userManager = (UserManagerInternal<CarbonPlayerCommon>) userManager;
        this.dataDirectory = dataDirectory;
        this.packetService();
    }

    void onEnable() {
        final Metrics metrics = new Metrics(this.plugin, BSTATS_PLUGIN_ID);

        for (final Class<? extends Listener> listenerClass : LISTENER_CLASSES) {
            this.plugin.getServer().getPluginManager().registerEvents(
                this.injector.getInstance(listenerClass),
                this.plugin
            );
        }

        // Listeners
        ListenerUtils.registerCommonListeners(this.injector);
        this.injector.getInstance(RadiusListener.class);

        // Commands
        // This is a bit awkward looking
        CloudUtils.loadCommands(this.injector);
        final var commandSettings = CloudUtils.loadCommandSettings(this.injector);
        CloudUtils.registerCommands(commandSettings);

        // Player data saving
        final long saveDelay = 5 * 60 * 20;

        Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin,
            () -> PlayerUtils.saveLoggedInPlayers(this.carbonServer, this.userManager, this.logger), saveDelay, saveDelay);

        // Load channels
        ((CarbonChannelRegistry) this.channelRegistry()).loadConfigChannels(this.carbonMessages);

        this.discoverDiscordHooks();
    }

    private void discoverDiscordHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord")) {
            final DiscordMessageListener discordMessageListener = this.injector.getInstance(DiscordMessageListener.class);
            Bukkit.getPluginManager().registerEvents(discordMessageListener, this.plugin);
            discordMessageListener.init();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            this.logger.info("DiscordSRV found! Enabling hook.");
            DiscordSRV.getPlugin().getPluginHooks().add(new DSRVChatHook());
        }
    }

    void onDisable() {
        this.injector.getInstance(ProfileResolver.class).shutdown();
        this.userManager.shutdown();
        this.injector.getInstance(ExecutionCoordinatorHolder.class).shutdown();
    }

    @Override
    public UUID serverId() {
        return this.serverId;
    }

    @Override
    public @Nullable PacketService packetService() {
        if (this.messagingManager == null) {
            this.messagingManager = this.injector.getInstance(MessagingManager.class);
        }

        return this.messagingManager.packetService();
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
    public CarbonServerPaper server() {
        return (CarbonServerPaper) this.carbonServer;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    public CarbonMessages carbonMessages() {
        return this.carbonMessages;
    }

    @Override
    public @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

    @Override
    public IMessageRenderer<Audience, String, Component, Component> messageRenderer() {
        return this.injector.getInstance(PaperMessageRenderer.class);
    }

    public static boolean papiLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

}
