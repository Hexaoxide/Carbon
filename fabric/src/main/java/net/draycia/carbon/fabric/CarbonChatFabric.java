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
import net.draycia.carbon.fabric.command.DeleteMessageCommand;
import net.draycia.carbon.fabric.listeners.FabricChatHandler;
import net.draycia.carbon.fabric.listeners.FabricJoinQuitListener;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonChatFabric extends CarbonChatInternal<CarbonPlayerFabric> {

    public static ResourceKey<ChatType> CHAT_TYPE = ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation("carbon", "chat"));

    @Inject
    private CarbonChatFabric(
        final Injector injector,
        final Logger logger,
        final @DataDirectory Path dataDirectory,
        final @PeriodicTasks ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final FabricUserManager userManager,
        final ExecutionCoordinatorHolder commandExecutor,
        final CarbonServer carbonServer,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler eventHandler,
        final CarbonChannelRegistry channelRegistry,
        final IMessageRenderer<Audience, String, Component, Component> renderer,
        final Provider<MessagingManager> messagingManagerProvider,
        @SuppressWarnings("unused") // Make sure it initializes now
        final MinecraftServerHolder minecraftServerHolder
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
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(this.injector().getInstance(FabricChatHandler.class));
    }

    private void registerServerLifecycleListeners() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.shutdown());
    }

    private void registerPlayerStatusListeners() {
        final FabricJoinQuitListener listener = this.injector().getInstance(FabricJoinQuitListener.class);
        ServerPlayConnectionEvents.DISCONNECT.register(listener);
        ServerPlayConnectionEvents.JOIN.register(listener);
    }

    public boolean luckPermsLoaded() {
        return FabricLoader.getInstance().isModLoaded("luckperms");
    }

}
