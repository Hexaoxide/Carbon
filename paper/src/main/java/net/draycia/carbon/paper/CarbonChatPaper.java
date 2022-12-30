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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import github.scarsz.discordsrv.DiscordSRV;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.common.CarbonChatInternal;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PeriodicTasks;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.commands.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.paper.hooks.DSRVChatHook;
import net.draycia.carbon.paper.listeners.DiscordMessageListener;
import net.draycia.carbon.paper.listeners.PaperChatListener;
import net.draycia.carbon.paper.listeners.PaperPlayerJoinListener;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonChatPaper extends CarbonChatInternal<CarbonPlayerPaper> {

    private static final Set<Class<? extends Listener>> LISTENER_CLASSES = Set.of(
        PaperChatListener.class,
        PaperPlayerJoinListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 8720;

    private final JavaPlugin plugin;

    @Inject
    private CarbonChatPaper(
        final Injector injector,
        final JavaPlugin plugin,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler eventHandler,
        final CarbonChannelRegistry channelRegistry,
        final Provider<MessagingManager> messagingManager,
        final CarbonServer carbonServer,
        final PaperUserManager userManager,
        @DataDirectory final Path dataDirectory,
        @PeriodicTasks final ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final ExecutionCoordinatorHolder commandExecutor,
        final IMessageRenderer<Audience, String, Component, Component> renderer
    ) {
        super(
            injector, LogManager.getLogger("CarbonChat"), dataDirectory,
            periodicTasks,
            profileCache,
            profileResolver,
            userManager,
            commandExecutor,
            carbonServer,
            carbonMessages,
            eventHandler,
            channelRegistry,
            renderer,
            messagingManager
        );
        this.plugin = plugin;
    }

    void onEnable() {
        final Metrics metrics = new Metrics(this.plugin, BSTATS_PLUGIN_ID);

        this.loadLibraries();

        this.init();
        this.packetService();

        for (final Class<? extends Listener> listenerClass : LISTENER_CLASSES) {
            this.plugin.getServer().getPluginManager().registerEvents(
                this.injector().getInstance(listenerClass),
                this.plugin
            );
        }

        this.discoverDiscordHooks();
    }

    private void loadLibraries() {
        final BukkitLibraryManager libraryManager = new BukkitLibraryManager(this.plugin);
        libraryManager.addMavenCentral();
        libraryManager.addSonatype();

        // TODO: move this into common, supply library manager
        final Library messenger = Library.builder()
            .groupId("com.github.luben")
            .artifactId("zstd-jni")
            .version("1.5.1-1")
            .relocate("com.github.luben", "net.draycia.carbon.libs.com.github.luben")
            .build();

        libraryManager.loadLibrary(messenger);

        final Library guava = Library.builder()
            .groupId("com.google.guava")
            .artifactId("guava")
            .version("30.1-jre")
            .relocate("com.google.common", "net.draycia.carbon.libs.com.google.common")
            .build();

        libraryManager.loadLibrary(guava);

        final Library protobuf = Library.builder()
            .groupId("com.google.protobuf")
            .artifactId("protobuf-java")
            .version("3.21.12")
            .relocate("com.google.protobuf", "net.draycia.carbon.libs.com.google.protobuf")
            .build();

        libraryManager.loadLibrary(protobuf);

        final Library mysqlConnector = Library.builder()
            .groupId("mysql")
            .artifactId("mysql-connector-j")
            .version("8.0.31")
            .relocate("mysql-connector-j", "net.draycia.carbon.libs.mysql")
            .build();

        libraryManager.loadLibrary(mysqlConnector);
    }

    private void discoverDiscordHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord")) {
            final DiscordMessageListener discordMessageListener = this.injector().getInstance(DiscordMessageListener.class);
            Bukkit.getPluginManager().registerEvents(discordMessageListener, this.plugin);
            discordMessageListener.init();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            this.logger().info("DiscordSRV found! Enabling hook.");
            DiscordSRV.getPlugin().getPluginHooks().add(new DSRVChatHook());
        }
    }

    void onDisable() {
        this.shutdown();
    }

    public static boolean papiLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

}
