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
import net.draycia.carbon.paper.hooks.CarbonPAPIPlaceholders;
import net.draycia.carbon.paper.hooks.DSRVChatHook;
import net.draycia.carbon.paper.hooks.PAPIChatHook;
import net.draycia.carbon.paper.listeners.DiscordMessageListener;
import net.draycia.carbon.paper.listeners.PaperChatListener;
import net.draycia.carbon.paper.listeners.PaperPlayerJoinListener;
import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonChatPaper extends CarbonChatInternal {

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
        final PlatformUserManager userManager,
        @PeriodicTasks final ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final ExecutionCoordinatorHolder commandExecutor
    ) {
        super(
            injector,
            LogManager.getLogger("CarbonChat"),
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
        this.plugin = plugin;
    }

    void onEnable() {
        this.init();
        this.packetService();

        for (final Class<? extends Listener> listenerClass : LISTENER_CLASSES) {
            this.plugin.getServer().getPluginManager().registerEvents(
                this.injector().getInstance(listenerClass),
                this.plugin
            );
        }

        this.discoverDiscordHooks();

        final Metrics metrics = new Metrics(this.plugin, BSTATS_PLUGIN_ID);
        // metrics.addCustomChart(new SimplePie("user_manager_type", () -> this.injector().getInstance(ConfigFactory.class).primaryConfig().storageType().name()));
    }

    private void discoverDiscordHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord")) {
            final DiscordMessageListener discordMessageListener = this.injector().getInstance(DiscordMessageListener.class);
            Bukkit.getPluginManager().registerEvents(discordMessageListener, this.plugin);
            discordMessageListener.init();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.getPlugin().getPluginHooks().add(this.injector().getInstance(DSRVChatHook.class));
        }

        if (papiLoaded()) {
            this.injector().getInstance(PAPIChatHook.class);
            this.injector().getInstance(CarbonPAPIPlaceholders.class);
        }
    }

    void onDisable() {
        this.shutdown();
    }

    public static boolean papiLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static boolean miniPlaceholdersLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders");
    }

}
