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

import cloud.commandframework.CommandManager;
import cloud.commandframework.fabric.FabricServerCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.commands.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.fabric.command.FabricCommander;
import net.draycia.carbon.fabric.command.FabricPlayerCommander;
import net.draycia.carbon.fabric.users.FabricProfileResolver;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabricModule extends AbstractModule {

    private final Logger logger;
    private final ModContainer modContainer;

    CarbonChatFabricModule() {
        final ModContainer modContainer = FabricLoader.getInstance().getModContainer("carbonchat")
            .orElseThrow(() -> new IllegalStateException("Could not find ModContainer for carbonchat."));
        this.modContainer = modContainer;
        this.logger = LogManager.getLogger(modContainer.getMetadata().getName());
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager(
        final ExecutionCoordinatorHolder executionCoordinatorHolder,
        final Provider<CarbonChatFabric> carbonChat,
        final CarbonMessages carbonMessages
    ) {
        final FabricServerCommandManager<Commander> commandManager = new FabricServerCommandManager<>(
            executionCoordinatorHolder.executionCoordinator(),
            commandSourceStack -> {
                if (commandSourceStack.getEntity() instanceof ServerPlayer) {
                    return new FabricPlayerCommander(carbonChat.get(), commandSourceStack);
                }
                return FabricCommander.from(commandSourceStack);
            },
            commander -> ((FabricCommander) commander).commandSourceStack()
        );

        CloudUtils.decorateCommandManager(commandManager, carbonMessages);

        commandManager.brigadierManager().setNativeNumberSuggestions(false);

        return commandManager;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(ModContainer.class).toInstance(this.modContainer);
        this.bind(CarbonChat.class).to(CarbonChatFabric.class);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(FabricLoader.getInstance().getConfigDir().resolve(this.modContainer.getMetadata().getId()));
        this.bind(CarbonServer.class).to(CarbonServerFabric.class);
        this.bind(ProfileResolver.class).to(FabricProfileResolver.class);
        this.bind(PlatformScheduler.class).to(FabricScheduler.class);
        this.bind(new TypeLiteral<UserManager<?>>() {}).to(FabricUserManager.class);
        this.bind(new TypeLiteral<UserManagerInternal<?>>() {}).to(FabricUserManager.class);
        this.bind(CarbonMessageRenderer.class).to(FabricMessageRenderer.class);
    }

}
