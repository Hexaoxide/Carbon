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
package net.draycia.carbon.sponge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import cloud.commandframework.sponge.argument.SinglePlayerSelectorArgument;
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
import net.draycia.carbon.sponge.command.SpongeCommander;
import net.draycia.carbon.sponge.command.SpongePlayerCommander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeModule extends AbstractModule {

    private final CarbonChatSponge carbonChat;
    private final Path configDir;
    private final PluginContainer pluginContainer;

    public CarbonChatSpongeModule(
        final CarbonChatSponge carbonChat,
        final Path configDir,
        final PluginContainer pluginContainer
    ) {
        this.carbonChat = carbonChat;
        this.configDir = configDir;
        this.pluginContainer = pluginContainer;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final SpongeCommandManager<Commander> commandManager = new SpongeCommandManager<>(
            this.pluginContainer,
            AsynchronousCommandExecutionCoordinator.<Commander>builder().build(),
            commander -> ((SpongeCommander) commander).commandCause(),
            commandCause -> {
                if (commandCause.subject() instanceof ServerPlayer player) {
                    return new SpongePlayerCommander(this.carbonChat, player, commandCause);
                }

                return SpongeCommander.from(commandCause);
            }
        );

        CloudUtils.decorateCommandManager(commandManager, this.carbonChat.carbonMessages());

        commandManager.parserMapper().cloudNumberSuggestions(true);

        return commandManager;
    }

    @Provides
    @Singleton
    public IMessageRenderer<Audience, String, RenderedMessage, Component> messageRenderer(final Injector injector) {
        return injector.getInstance(SpongeMessageRenderer.class);
    }

    @Provides
    @Singleton
    public IMessageRenderer<SourcedAudience, String, RenderedMessage, Component> sourcedRenderer(final Injector injector) {
        return injector.getInstance(SpongeMessageRenderer.class);
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.configDir);
        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
        this.bind(PlayerSuggestions.class).toInstance(new SinglePlayerSelectorArgument.Parser<Commander>()::suggestions);
    }

}
