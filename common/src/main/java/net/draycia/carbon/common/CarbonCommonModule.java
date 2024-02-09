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
package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider3;
import com.google.inject.multibindings.Multibinder;
import io.leangen.geantyref.TypeToken;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.command.ParserFactory;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.command.commands.ClearChatCommand;
import net.draycia.carbon.common.command.commands.ContinueCommand;
import net.draycia.carbon.common.command.commands.DebugCommand;
import net.draycia.carbon.common.command.commands.HelpCommand;
import net.draycia.carbon.common.command.commands.IgnoreCommand;
import net.draycia.carbon.common.command.commands.IgnoreListCommand;
import net.draycia.carbon.common.command.commands.JoinCommand;
import net.draycia.carbon.common.command.commands.LeaveCommand;
import net.draycia.carbon.common.command.commands.MuteCommand;
import net.draycia.carbon.common.command.commands.MuteInfoCommand;
import net.draycia.carbon.common.command.commands.NicknameCommand;
import net.draycia.carbon.common.command.commands.PartyCommands;
import net.draycia.carbon.common.command.commands.ReloadCommand;
import net.draycia.carbon.common.command.commands.ReplyCommand;
import net.draycia.carbon.common.command.commands.ToggleMessagesCommand;
import net.draycia.carbon.common.command.commands.UnignoreCommand;
import net.draycia.carbon.common.command.commands.UnmuteCommand;
import net.draycia.carbon.common.command.commands.UpdateUsernameCommand;
import net.draycia.carbon.common.command.commands.WhisperCommand;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.event.CarbonEventHandlerImpl;
import net.draycia.carbon.common.listeners.DeafenHandler;
import net.draycia.carbon.common.listeners.HyperlinkHandler;
import net.draycia.carbon.common.listeners.IgnoreHandler;
import net.draycia.carbon.common.listeners.ItemLinkHandler;
import net.draycia.carbon.common.listeners.Listener;
import net.draycia.carbon.common.listeners.MessagePacketHandler;
import net.draycia.carbon.common.listeners.MuteHandler;
import net.draycia.carbon.common.listeners.PingHandler;
import net.draycia.carbon.common.listeners.RadiusListener;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.Option;
import net.draycia.carbon.common.messages.SourcedReceiverResolver;
import net.draycia.carbon.common.messages.StandardPlaceholderResolverStrategyButDifferent;
import net.draycia.carbon.common.messages.placeholders.BooleanPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.IntPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.OptionPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.UUIDPlaceholderResolver;
import net.draycia.carbon.common.messaging.ServerId;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.Backing;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.NetworkUsers;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.users.db.argument.BinaryUUIDArgumentFactory;
import net.draycia.carbon.common.users.db.mapper.BinaryUUIDColumnMapper;
import net.draycia.carbon.common.users.db.mapper.NativeUUIDColumnMapper;
import net.draycia.carbon.common.users.json.JSONUserManager;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.draycia.carbon.common.util.Exceptions;
import net.draycia.carbon.common.util.FileUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.spongepowered.configurate.util.NamingSchemes;

@DefaultQualifier(NonNull.class)
public final class CarbonCommonModule extends AbstractModule {

    @Provides
    @Backing
    @Singleton
    public UserManagerInternal<CarbonPlayerCommon> userManager(
        final @DataDirectory Path dataDirectory,
        final ConfigManager configManager,
        final Logger logger,
        final Injector injector
    ) throws IOException {
        logger.info("Initializing " + configManager.primaryConfig().storageType() + " storage manager...");

        return switch (configManager.primaryConfig().storageType()) {
            case MYSQL -> injector.getInstance(DatabaseUserManager.Factory.class).create(
                "queries/migrations/mysql",
                jdbi -> jdbi.registerArgument(new BinaryUUIDArgumentFactory())
                    .registerColumnMapper(UUID.class, new BinaryUUIDColumnMapper())
            );
            case PSQL -> injector.getInstance(DatabaseUserManager.Factory.class).create(
                "queries/migrations/postgresql",
                jdbi -> jdbi.registerColumnMapper(UUID.class, new NativeUUIDColumnMapper())
                    .installPlugin(new PostgresPlugin())
            );
            case H2 -> injector.getInstance(DatabaseUserManager.Factory.class).create(
                "queries/migrations/h2",
                jdbi -> jdbi.installPlugin(new H2DatabasePlugin()),
                new DatabaseSettings("jdbc:h2:" + FileUtil.mkParentDirs(dataDirectory.resolve("users/userdata-h2")).toAbsolutePath() + ";MODE=MySQL", "", "")
            );
            case JSON -> injector.getInstance(JSONUserManager.class);
        };
    }

    @Provides
    @PeriodicTasks
    @Singleton
    public ScheduledExecutorService periodicTasksExecutor(final Logger logger) {
        return ConcurrentUtil.createPeriodicTasksPool(logger);
    }

    @Provides
    @Singleton
    public CarbonMessages carbonMessages(
        final SourcedReceiverResolver receiverResolver,
        final ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
        final UUIDPlaceholderResolver<Audience> uuidPlaceholderResolver,
        final StringPlaceholderResolver<Audience> stringPlaceholderResolver,
        final IntPlaceholderResolver<Audience> intPlaceholderResolver,
        final KeyPlaceholderResolver<Audience> keyPlaceholderResolver,
        final BooleanPlaceholderResolver<Audience> booleanPlaceholderResolver,
        final CarbonMessageSource carbonMessageSource,
        final CarbonMessageSender carbonMessageSender,
        final CarbonMessageRenderer messageRenderer
    ) throws UnscannableMethodException {
        return Moonshine.<CarbonMessages, Audience>builder(new TypeToken<>() {})
            .receiverLocatorResolver(receiverResolver, 0)
            .sourced(carbonMessageSource)
            .rendered(messageRenderer)
            .sent(carbonMessageSender)
            .resolvingWithStrategy(new StandardPlaceholderResolverStrategyButDifferent<>(NamingSchemes.SNAKE_CASE))
            .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
            .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
            .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Integer.class, intPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Boolean.class, booleanPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Option.class, new OptionPlaceholderResolver<>(), 0)
            .create(this.getClass().getClassLoader());
    }

    @Provides
    @Singleton
    public ExecutionCoordinatorHolder executionCoordinatorHolder(final Logger logger) {
        return ExecutionCoordinatorHolder.create(logger);
    }

    @Override
    protected void configure() {
        this.install(new FactoryModuleBuilder().build(ParserFactory.class));
        this.install(factoryModule(PacketFactory.class));
        this.bind(ServerId.KEY).toInstance(UUID.randomUUID());
        this.bind(ChannelRegistry.class).to(CarbonChannelRegistry.class);
        this.bind(CarbonEventHandler.class).to(CarbonEventHandlerImpl.class);
        this.bind(PlayerSuggestions.class).to(NetworkUsers.class);
        this.bind(new TypeLiteral<UserManager<?>>() {}).to(PlatformUserManager.class);
        this.bind(new TypeLiteral<UserManagerInternal<?>>() {}).to(PlatformUserManager.class);

        this.configureListeners();
        this.configureCommands();
    }

    private void configureListeners() {
        final Multibinder<Listener> listeners = Multibinder.newSetBinder(this.binder(), Listener.class);
        listeners.addBinding().to(DeafenHandler.class);
        listeners.addBinding().to(HyperlinkHandler.class);
        listeners.addBinding().to(IgnoreHandler.class);
        listeners.addBinding().to(ItemLinkHandler.class);
        listeners.addBinding().to(MessagePacketHandler.class);
        listeners.addBinding().to(MuteHandler.class);
        listeners.addBinding().to(PingHandler.class);
        listeners.addBinding().to(RadiusListener.class);
    }

    private void configureCommands() {
        this.requestStaticInjection(CloudUtils.class);

        final Multibinder<CarbonCommand> commands = Multibinder.newSetBinder(this.binder(), CarbonCommand.class);
        commands.addBinding().to(ClearChatCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(ContinueCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(DebugCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(HelpCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(IgnoreCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(MuteCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(MuteInfoCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(NicknameCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(ReloadCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(ReplyCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(ToggleMessagesCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(UnignoreCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(UnmuteCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(UpdateUsernameCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(WhisperCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(JoinCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(LeaveCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(IgnoreListCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(PartyCommands.class).in(Scopes.SINGLETON);
    }

    // Helper to create a FactoryProvider3 module
    private static <T> Module factoryModule(final Class<T> factoryInterface) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                try {
                    final Provider<T> factoryProvider = new FactoryProvider3<>(
                        com.google.inject.Key.get(TypeLiteral.get(factoryInterface)),
                        null,
                        MethodHandles.privateLookupIn(factoryInterface, MethodHandles.lookup())
                    );
                    this.binder().bind(factoryInterface).toProvider(factoryProvider);
                } catch (final Exception e) {
                    throw Exceptions.rethrow(e);
                }
            }
        };
    }

}
