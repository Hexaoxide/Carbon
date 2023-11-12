/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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

import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mojang.brigadier.tree.CommandNode;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.CarbonPlatformModule;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.fabric.command.FabricCommander;
import net.draycia.carbon.fabric.command.FabricPlayerCommander;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.draycia.carbon.fabric.users.FabricProfileResolver;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.key.Key;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.fabric.FabricServerCommandManager;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabricModule extends CarbonPlatformModule {

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
        // Remove existing commands matching our commands or aliases
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            final Map<Key, CommandSettings> settings = carbonChat.get().injector().getInstance(ConfigManager.class).loadCommandSettings();
            final Iterator<CommandNode<CommandSourceStack>> it = dispatcher.getRoot().getChildren().iterator();
            while (it.hasNext()) {
                final CommandNode<CommandSourceStack> next = it.next();
                final String name = next.getName();
                if (settings.values().stream().anyMatch(s -> s.name().equals(name) || Arrays.asList(s.aliases()).contains(name))) {
                    it.remove();
                }
            }
        });

        final FabricServerCommandManager<Commander> commandManager = new FabricServerCommandManager<>(
            executionCoordinatorHolder.executionCoordinator(),
            SenderMapper.create(
                commandSourceStack -> {
                    if (commandSourceStack.getEntity() instanceof ServerPlayer) {
                        return new FabricPlayerCommander(carbonChat.get(), commandSourceStack);
                    }
                    return FabricCommander.from(commandSourceStack);
                },
                commander -> ((FabricCommander) commander).commandSourceStack()
            )
        );

        CloudUtils.decorateCommandManager(commandManager, carbonMessages);

        commandManager.brigadierManager().setNativeNumberSuggestions(false);

        return commandManager;
    }

    @Override
    protected void configurePlatform() {
        this.install(new CarbonCommonModule());

        this.bind(ModContainer.class).toInstance(this.modContainer);
        this.bind(CarbonChat.class).to(CarbonChatFabric.class);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(FabricLoader.getInstance().getConfigDir().resolve(this.modContainer.getMetadata().getId()));
        this.bind(CarbonServer.class).to(CarbonServerFabric.class);
        this.bind(ProfileResolver.class).to(FabricProfileResolver.class);
        this.bind(PlatformScheduler.class).to(FabricScheduler.class);
        this.install(PlatformUserManager.PlayerFactory.moduleFor(CarbonPlayerFabric.class));
        this.bind(CarbonMessageRenderer.class).to(FabricMessageRenderer.class);
    }

}
