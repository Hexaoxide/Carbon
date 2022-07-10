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

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.fabric.argument.FabricArgumentParsers;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.fabric.command.FabricCommander;
import net.draycia.carbon.fabric.command.FabricPlayerCommander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabricModule extends AbstractModule {

    private final Logger logger;
    private final CarbonChatFabric carbonChat;
    private final Path dataDirectory;

    CarbonChatFabricModule(
        final CarbonChatFabric carbonChat,
        final Logger logger,
        final Path dataDirectory
    ) {
        this.logger = logger;
        this.carbonChat = carbonChat;
        this.dataDirectory = dataDirectory;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final FabricServerCommandManager<Commander> commandManager = new FabricServerCommandManager<>(
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            FabricCommander::from,
            commander -> ((FabricCommander) commander).commandSourceStack()
        );

        CloudUtils.decorateCommandManager(commandManager, this.carbonChat.carbonMessages());

        commandManager.brigadierManager().setNativeNumberSuggestions(false);

        return commandManager;
    }

    @Provides
    @Singleton
    public IMessageRenderer<Audience, String, RenderedMessage, Component> messageRenderer(final Injector injector) {
        return injector.getInstance(FabricMessageRenderer.class);
    }

    @Provides
    @Singleton
    public IMessageRenderer<SourcedAudience, String, RenderedMessage, Component> sourcedRenderer(final Injector injector) {
        return injector.getInstance(FabricMessageRenderer.class);
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonChatFabric.class).toInstance(this.carbonChat);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerFabric.class);
        this.bind(PlayerSuggestions.class).toInstance(FabricArgumentParsers.<Commander>singlePlayerSelector()::suggestions);
    }

}
