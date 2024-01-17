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
package net.draycia.carbon.paper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.CarbonPlatformModule;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.command.argument.SignedGreedyStringParser;
import net.draycia.carbon.common.integration.Integration;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.paper.command.PaperCommander;
import net.draycia.carbon.paper.command.PaperPlayerCommander;
import net.draycia.carbon.paper.integration.fuuid.FactionsIntegration;
import net.draycia.carbon.paper.integration.mcmmo.McmmoIntegration;
import net.draycia.carbon.paper.integration.towny.TownyIntegration;
import net.draycia.carbon.paper.listeners.PaperChatListener;
import net.draycia.carbon.paper.listeners.PaperPlayerJoinListener;
import net.draycia.carbon.paper.messages.PaperMessageRenderer;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import net.draycia.carbon.paper.users.PaperProfileResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.paper.PaperCommandManager;

@DefaultQualifier(NonNull.class)
public final class CarbonChatPaperModule extends CarbonPlatformModule {

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonPaperBootstrap bootstrap;

    CarbonChatPaperModule(final CarbonPaperBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    public CommandManager<Commander> commandManager(final UserManager<?> userManager, final CarbonMessages messages, final ExecutionCoordinatorHolder executionCoordinatorHolder) {
        final PaperCommandManager<Commander> commandManager = new PaperCommandManager<>(
            this.bootstrap,
            executionCoordinatorHolder.executionCoordinator(),
            SenderMapper.create(
                commandSender -> {
                    if (commandSender instanceof Player player) {
                        return new PaperPlayerCommander(userManager, player);
                    }
                    return PaperCommander.from(commandSender);
                },
                commander -> ((PaperCommander) commander).commandSender()
            )
        );

        CloudUtils.decorateCommandManager(commandManager, messages, this.logger);

        commandManager.registerBrigadier();

        return commandManager;
    }

    @Override
    protected void configurePlatform() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatPaper.class);
        this.bind(JavaPlugin.class).toInstance(this.bootstrap);
        this.bind(Server.class).toInstance(this.bootstrap.getServer());
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(this.bootstrap.getDataFolder().toPath());
        this.bind(CarbonServer.class).to(CarbonServerPaper.class);
        this.bind(ProfileResolver.class).to(PaperProfileResolver.class);
        this.bind(PlatformScheduler.class).to(PaperScheduler.class);
        this.install(PlatformUserManager.PlayerFactory.moduleFor(CarbonPlayerPaper.class));
        this.bind(CarbonMessageRenderer.class).to(PaperMessageRenderer.class);
        this.bind(SignedGreedyStringParser.Mapper.class).to(SignedGreedyStringParser.NonSignedMapper.class);

        this.configureListeners();
    }

    @Override
    protected void configureIntegrations(final Multibinder<Integration> integrations, final Multibinder<Integration.ConfigMeta> configs) {
        integrations.addBinding().to(TownyIntegration.class);
        configs.addBinding().toInstance(TownyIntegration.configMeta());

        integrations.addBinding().to(McmmoIntegration.class);
        configs.addBinding().toInstance(McmmoIntegration.configMeta());

        integrations.addBinding().to(FactionsIntegration.class);
        configs.addBinding().toInstance(FactionsIntegration.configMeta());
    }

    private void configureListeners() {
        final Multibinder<Listener> listeners = Multibinder.newSetBinder(this.binder(), Listener.class);
        listeners.addBinding().to(PaperChatListener.class);
        listeners.addBinding().to(PaperPlayerJoinListener.class);
    }

}
