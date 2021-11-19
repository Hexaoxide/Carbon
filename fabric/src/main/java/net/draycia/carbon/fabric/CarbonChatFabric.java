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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ListenerUtils;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.fabric.callback.ChatCallback;
import net.draycia.carbon.fabric.listeners.FabricChatListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabric implements ModInitializer, CarbonChat {

    private final CarbonEventHandler eventHandler = new CarbonEventHandler();
    private @Nullable MinecraftServer minecraftServer;
    private @MonotonicNonNull ModContainer modContainer;
    private @MonotonicNonNull Injector injector;
    private @MonotonicNonNull UserManager<CarbonPlayerCommon> userManager;
    private @MonotonicNonNull Logger logger;
    private @MonotonicNonNull CarbonServerFabric carbonServerFabric;
    private @MonotonicNonNull CarbonMessageService messageService;
    private @MonotonicNonNull ChannelRegistry channelRegistry;

    @Override
    public void onInitialize() {
        this.modContainer = FabricLoader.getInstance().getModContainer("carbonchat").orElseThrow(() ->
            new IllegalStateException("Could not find ModContainer for carbonchat."));

        CarbonChatProvider.register(this);

        this.logger = LogManager.getLogger(this.modContainer.getMetadata().getName());
        this.injector = Guice.createInjector(new CarbonChatFabricModule(this, this.logger, this.dataDirectory()));
        this.messageService = this.injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServerFabric = this.injector.getInstance(CarbonServerFabric.class);
        this.userManager = this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<UserManager<CarbonPlayerCommon>>() {}));

        // Platform Listeners
        this.registerChatListener();
        this.registerServerLifecycleListeners();
        this.registerTickListeners();

        // Listeners
        ListenerUtils.registerCommonListeners(this.injector);

        // Commands
        CloudUtils.registerCommands(this.injector);

        // Load channels
        ((CarbonChannelRegistry) this.channelRegistry()).loadChannels();
    }

    private void registerChatListener() {
        ChatCallback.setup();
        ChatCallback.INSTANCE.registerListener(new FabricChatListener(this, this.channelRegistry));
    }

    private void registerServerLifecycleListeners() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.minecraftServer = server);
        ServerLifecycleEvents.SERVER_STOPPING.register($ -> PlayerUtils.saveLoggedInPlayers(this.carbonServerFabric, this.userManager).forEach(CompletableFuture::join));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.minecraftServer = null);
    }

    private void registerTickListeners() {
        final long saveDelay = 5 * 60 * 20; // 5 minutes

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() != 0 && server.getTickCount() % saveDelay == 0) {
                PlayerUtils.saveLoggedInPlayers(this.carbonServerFabric, this.userManager);
            }
        });
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path dataDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve(this.modContainer.getMetadata().getId());
    }

    @Override
    public CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

    @Override
    public CarbonServer server() {
        return this.carbonServerFabric;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    @Override
    public IMessageRenderer<SourcedAudience, String, RenderedMessage, Component> messageRenderer() {
        return this.injector.getInstance(FabricMessageRenderer.class);
    }

    public MinecraftServer minecraftServer() {
        return requireNonNull(this.minecraftServer, "Attempted to get the MinecraftServer instance when one is not active.");
    }

    public CarbonMessageService messageService() {
        return this.messageService;
    }

}
