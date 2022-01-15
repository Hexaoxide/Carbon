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
package net.draycia.carbon.bukkit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.papermc.lib.PaperLib;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.bukkit.listeners.BukkitChatListener;
import net.draycia.carbon.bukkit.listeners.BukkitPlayerJoinListener;
import net.draycia.carbon.bukkit.listeners.DiscordMessageListener;
import net.draycia.carbon.bukkit.util.BukkitMessageRenderer;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.listeners.RadiusListener;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ListenerUtils;
import net.draycia.carbon.common.util.PlayerUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkit extends JavaPlugin implements CarbonChat {

    private static final Set<Class<? extends Listener>> LISTENER_CLASSES = Set.of(
        BukkitChatListener.class,
        BukkitPlayerJoinListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 8720;
    private final CarbonEventHandler eventHandler = new CarbonEventHandler();
    private @MonotonicNonNull Injector injector;
    private @MonotonicNonNull UserManager<CarbonPlayerCommon> userManager;
    private @MonotonicNonNull Logger logger;
    private @MonotonicNonNull CarbonServerBukkit carbonServerBukkit;
    private @MonotonicNonNull CarbonMessageService messageService;
    private @MonotonicNonNull ChannelRegistry channelRegistry;

    @Override
    public void onLoad() {
        if (!PaperLib.isPaper()) {
            this.getLogger().log(Level.SEVERE, "*");
            this.getLogger().log(Level.SEVERE, "* CarbonChat makes extensive use of APIs added by Paper.");
            this.getLogger().log(Level.SEVERE, "* For this reason, CarbonChat is not compatible with Spigot or CraftBukkit servers.");
            this.getLogger().log(Level.SEVERE, "* Upgrade your server to Paper in order to use CarbonChat.");
            this.getLogger().log(Level.SEVERE, "*");
            PaperLib.suggestPaper(this, Level.SEVERE);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        CarbonChatProvider.register(this);

        this.injector = Guice.createInjector(new CarbonChatBukkitModule(this, this.dataDirectory()));
        this.logger = LogManager.getLogger("CarbonChat");
        this.messageService = this.injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServerBukkit = this.injector.getInstance(CarbonServerBukkit.class);
        this.userManager = this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<UserManager<CarbonPlayerCommon>>() {}));
    }

    @Override
    public void onEnable() {
        final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        for (final Class<? extends Listener> listenerClass : LISTENER_CLASSES) {
            this.getServer().getPluginManager().registerEvents(
                this.injector.getInstance(listenerClass),
                this
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

        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
            () -> PlayerUtils.saveLoggedInPlayers(this.carbonServerBukkit, this.userManager), saveDelay, saveDelay);

        // Load channels
        ((CarbonChannelRegistry) this.channelRegistry()).loadConfigChannels();

        this.discoverDiscordHooks();
    }

    private void discoverDiscordHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord")) {
            final DiscordMessageListener discordMessageListener = this.injector.getInstance(DiscordMessageListener.class);
            Bukkit.getPluginManager().registerEvents(discordMessageListener, this);
            discordMessageListener.init();
        }
    }

    @Override
    public void onDisable() {
        PlayerUtils.saveLoggedInPlayers(this.carbonServerBukkit, this.userManager).forEach(CompletableFuture::join);
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path dataDirectory() {
        return this.getDataFolder().toPath();
    }

    @Override
    public CarbonServerBukkit server() {
        return this.carbonServerBukkit;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    public CarbonMessageService messageService() {
        return this.messageService;
    }

    @Override
    public @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

    @Override
    public <T extends Audience> IMessageRenderer<T, String, RenderedMessage, Component> messageRenderer() {
        return this.injector.getInstance(BukkitMessageRenderer.class);
    }

    public boolean papiLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

}
