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
package net.draycia.carbon.velocity;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.leangen.geantyref.TypeToken;
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
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.velocity.command.VelocityCommander;
import net.draycia.carbon.velocity.command.VelocityPlayerCommander;
import net.draycia.carbon.velocity.listeners.VelocityChatListener;
import net.draycia.carbon.velocity.listeners.VelocityListener;
import net.draycia.carbon.velocity.listeners.VelocityPlayerJoinListener;
import net.draycia.carbon.velocity.listeners.VelocityPlayerLeaveListener;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import net.draycia.carbon.velocity.users.VelocityProfileResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.velocity.VelocityCommandManager;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

@DefaultQualifier(NonNull.class)
public final class CarbonChatVelocityModule extends CarbonPlatformModule {

    private final Logger logger = LogManager.getLogger("carbonchat");
    private final PluginContainer pluginContainer;
    private final ProxyServer proxyServer;
    private final Path dataDirectory;

    CarbonChatVelocityModule(
        final PluginContainer pluginContainer,
        final ProxyServer proxyServer,
        final Path dataDirectory
    ) {
        this.pluginContainer = pluginContainer;
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> createCommandManager(
        final ExecutionCoordinatorHolder executionCoordinatorHolder,
        final UserManager<?> userManager,
        final CarbonMessages messages
    ) {
        final VelocityCommandManager<Commander> commandManager = new VelocityCommandManager<>(
            this.pluginContainer,
            this.proxyServer,
            executionCoordinatorHolder.executionCoordinator(),
            SenderMapper.create(
                commandSender -> {
                    if (commandSender instanceof Player player) {
                        return new VelocityPlayerCommander(userManager, player);
                    }

                    return VelocityCommander.from(commandSender);
                },
                commander -> ((VelocityCommander) commander).commandSource()
            )
        );

        CloudUtils.decorateCommandManager(commandManager, messages, this.logger);

        commandManager.brigadierManager().registerMapping(
            TypeToken.get(SignedGreedyStringParser.class),
            builder -> builder.toConstant(greedyString()).cloudSuggestions()
        );

        return commandManager;
    }

    @Override
    protected void configurePlatform() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatVelocity.class);
        this.bind(CarbonServer.class).to(CarbonServerVelocity.class);
        this.bind(ProfileResolver.class).to(VelocityProfileResolver.class);
        this.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(this.dataDirectory);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(PlatformScheduler.class).to(PlatformScheduler.RunImmediately.class);
        this.install(PlatformUserManager.PlayerFactory.moduleFor(CarbonPlayerVelocity.class));
        this.bind(CarbonMessageRenderer.class).to(VelocityMessageRenderer.class);
        this.bind(SignedGreedyStringParser.Mapper.class).to(SignedGreedyStringParser.NonSignedMapper.class);

        this.configureListeners();
    }

    private void configureListeners() {
        final Multibinder<VelocityListener<?>> listeners = Multibinder.newSetBinder(this.binder(), new TypeLiteral<VelocityListener<?>>() {});
        listeners.addBinding().to(VelocityChatListener.class);
        listeners.addBinding().to(VelocityPlayerJoinListener.class);
        listeners.addBinding().to(VelocityPlayerLeaveListener.class);
    }

}
