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
package net.draycia.carbon.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.velocity.command.VelocityCommander;
import net.draycia.carbon.velocity.command.VelocityPlayerCommander;
import net.draycia.carbon.velocity.users.VelocityProfileResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatVelocityModule extends AbstractModule {

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
    public CommandManager<Commander> createCommandManager(final UserManager<?> userManager) {
        final VelocityCommandManager<Commander> commandManager = new VelocityCommandManager<>(
            this.pluginContainer,
            this.proxyServer,
            AsynchronousCommandExecutionCoordinator.<Commander>builder().build(),
            commandSender -> {
                if (commandSender instanceof Player player) {
                    return new VelocityPlayerCommander(userManager, player);
                }

                return VelocityCommander.from(commandSender);
            },
            commander -> ((VelocityCommander) commander).commandSource()
        );

        final var brigadierManager = commandManager.brigadierManager();
        brigadierManager.setNativeNumberSuggestions(false);

        return commandManager;
    }

    @Provides
    @Singleton
    public IMessageRenderer<Audience, String, Component, Component> messageRenderer(final Injector injector) {
        return injector.getInstance(VelocityMessageRenderer.class);
    }

    @Provides
    @Singleton
    public IMessageRenderer<SourcedAudience, String, Component, Component> sourcedRenderer(final Injector injector) {
        return injector.getInstance(VelocityMessageRenderer.class);
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatVelocity.class);
        this.bind(CarbonServer.class).to(CarbonServerVelocity.class);
        this.bind(PlayerSuggestions.class).toInstance(new PlayerArgument.PlayerParser<Commander>()::suggestions);
        this.bind(ProfileResolver.class).to(VelocityProfileResolver.class);
        this.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(this.dataDirectory);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(PlatformScheduler.class).to(PlatformScheduler.RunImmediately.class);
        this.bind(new TypeLiteral<UserManager<?>>() {}).to(VelocityUserManager.class);
        this.bind(new TypeLiteral<UserManagerInternal<?>>() {}).to(VelocityUserManager.class);
    }

}
