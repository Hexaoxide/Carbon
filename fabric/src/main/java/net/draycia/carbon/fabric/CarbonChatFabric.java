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
package net.draycia.carbon.fabric;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.common.CarbonChatInternal;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PeriodicTasks;
import net.draycia.carbon.common.command.commands.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.fabric.callback.ChatCallback;
import net.draycia.carbon.fabric.command.DeleteMessageCommand;
import net.draycia.carbon.fabric.listeners.FabricChatListener;
import net.draycia.carbon.fabric.listeners.FabricChatPreviewListener;
import net.draycia.carbon.fabric.listeners.FabricPlayerJoinListener;
import net.draycia.carbon.fabric.listeners.FabricPlayerLeaveListener;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonChatFabric extends CarbonChatInternal<CarbonPlayerFabric> {

    private @Nullable MinecraftServer minecraftServer;

    public static ResourceKey<ChatType> CHAT_TYPE = ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation("carbon", "chat"));

    @Inject
    private CarbonChatFabric(
        final Injector injector,
        final Logger logger,
        final @DataDirectory Path dataDirectory,
        final @PeriodicTasks ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final UserManagerInternal<CarbonPlayerFabric> userManager,
        final ExecutionCoordinatorHolder commandExecutor,
        final CarbonServer carbonServer,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler eventHandler,
        final ChannelRegistry channelRegistry,
        final IMessageRenderer<Audience, String, Component, Component> renderer,
        final Provider<MessagingManager> messagingManagerProvider
    ) {
        super(
            injector,
            logger,
            dataDirectory,
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
            messagingManagerProvider
        );
    }

    public void onInitialize() {
        this.init();

        // Platform Listeners
        this.registerChatListener();
        this.registerServerLifecycleListeners();
        this.registerPlayerStatusListeners();

        this.injector().getInstance(DeleteMessageCommand.class).init();
    }

    private void registerChatListener() {
        ChatCallback.setup();
        ChatCallback.INSTANCE.registerListener(new FabricChatListener(this, this.channelRegistry()));
        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, this.injector().getInstance(FabricChatPreviewListener.class));
    }

    private void registerServerLifecycleListeners() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.minecraftServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            this.shutdown();
            this.minecraftServer = null;
        });
    }

    private void registerPlayerStatusListeners() {
        ServerPlayConnectionEvents.DISCONNECT.register(this.injector().getInstance(FabricPlayerLeaveListener.class));
        ServerPlayConnectionEvents.JOIN.register(this.injector().getInstance(FabricPlayerJoinListener.class));
    }

    public MinecraftServer minecraftServer() {
        return requireNonNull(this.minecraftServer, "Attempted to get the MinecraftServer instance when one is not active.");
    }

    public boolean luckPermsLoaded() {
        return FabricLoader.getInstance().isModLoaded("luckperms");
    }

}
