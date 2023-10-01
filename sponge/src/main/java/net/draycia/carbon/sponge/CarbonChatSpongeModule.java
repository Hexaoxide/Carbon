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
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.sponge.command.SpongeCommander;
import net.draycia.carbon.sponge.command.SpongePlayerCommander;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import net.draycia.carbon.sponge.users.SpongeProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeModule extends AbstractModule {

    private final Path configDir;
    private final PluginContainer pluginContainer;

    public CarbonChatSpongeModule(
        final Path configDir,
        final PluginContainer pluginContainer
    ) {
        this.configDir = configDir;
        this.pluginContainer = pluginContainer;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager(
        final ExecutionCoordinatorHolder executionCoordinatorHolder,
        final Provider<CarbonChatSponge> carbonChat,
        final CarbonMessages carbonMessages
    ) {
        final SpongeCommandManager<Commander> commandManager = new SpongeCommandManager<>(
            this.pluginContainer,
            executionCoordinatorHolder.executionCoordinator(),
            commander -> ((SpongeCommander) commander).commandCause(),
            commandCause -> {
                if (commandCause.subject() instanceof ServerPlayer player) {
                    return new SpongePlayerCommander(carbonChat.get(), player, commandCause);
                }

                return SpongeCommander.from(commandCause);
            }
        );

        CloudUtils.decorateCommandManager(commandManager, carbonMessages);

        commandManager.parserMapper().cloudNumberSuggestions(true);

        return commandManager;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatSponge.class);
        this.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(this.configDir);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
        this.bind(CarbonMessageRenderer.class).to(SpongeMessageRenderer.class);
        this.bind(ProfileResolver.class).to(SpongeProfileResolver.class);
        this.bind(PlatformScheduler.class).to(SpongeScheduler.class);
        this.install(PlatformUserManager.PlayerFactory.moduleFor(CarbonPlayerSponge.class));
        this.bind(CarbonMessageRenderer.class).to(SpongeMessageRenderer.class);
    }

}
