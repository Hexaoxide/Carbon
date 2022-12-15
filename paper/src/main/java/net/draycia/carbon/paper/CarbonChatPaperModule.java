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
package net.draycia.carbon.paper;

import cloud.commandframework.CommandManager;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
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
import net.draycia.carbon.paper.command.PaperCommander;
import net.draycia.carbon.paper.command.PaperPlayerCommander;
import net.draycia.carbon.paper.util.PaperMessageRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatPaperModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonChatPaper carbonChat;
    private final Path dataDirectory;

    CarbonChatPaperModule(
        final CarbonChatPaper carbonChat,
        final Path dataDirectory
    ) {
        this.carbonChat = carbonChat;
        this.dataDirectory = dataDirectory;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final PaperCommandManager<Commander> commandManager;

        try {
            commandManager = new PaperCommandManager<>(
                this.carbonChat,
                AsynchronousCommandExecutionCoordinator.<Commander>builder().build(),
                commandSender -> {
                    if (commandSender instanceof Player player) {
                        return new PaperPlayerCommander(this.carbonChat, player);
                    }
                    return PaperCommander.from(commandSender);
                },
                commander -> ((PaperCommander) commander).commandSender()
            );
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to initialize command manager.", ex);
        }

        CloudUtils.decorateCommandManager(commandManager, this.carbonChat.carbonMessages());

        commandManager.registerAsynchronousCompletions();
        commandManager.registerBrigadier();

        final @Nullable CloudBrigadierManager<Commander, ?> brigadierManager =
            commandManager.brigadierManager();

        if (brigadierManager != null) {
            brigadierManager.setNativeNumberSuggestions(false);
        }

        return commandManager;
    }

    @Provides
    @Singleton
    public IMessageRenderer<Audience, String, RenderedMessage, Component> messageRenderer(final Injector injector) {
        return injector.getInstance(PaperMessageRenderer.class);
    }

    @Provides
    @Singleton
    public IMessageRenderer<SourcedAudience, String, RenderedMessage, Component> sourcedRenderer(final Injector injector) {
        return injector.getInstance(PaperMessageRenderer.class);
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonChatPaper.class).toInstance(this.carbonChat);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerPaper.class);
        this.bind(PlayerSuggestions.class).toInstance(new PlayerArgument.PlayerParser<Commander>()::suggestions);
    }

}
