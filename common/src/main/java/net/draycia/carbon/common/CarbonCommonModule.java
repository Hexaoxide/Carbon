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
package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.commands.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.SourcedReceiverResolver;
import net.draycia.carbon.common.messages.StandardPlaceholderResolverStrategyButDifferent;
import net.draycia.carbon.common.messages.placeholders.BooleanPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.UUIDPlaceholderResolver;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.Backing;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.users.db.mysql.MySQLUserManager;
import net.draycia.carbon.common.users.db.postgresql.PostgreSQLUserManager;
import net.draycia.carbon.common.users.json.JSONUserManager;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonCommonModule extends AbstractModule {

    @Provides
    @Backing
    @Singleton
    public UserManagerInternal<CarbonPlayerCommon> userManager(final ConfigFactory configFactory, final Injector injector) {
        return switch (Objects.requireNonNull(configFactory.primaryConfig()).storageType()) {
            case MYSQL -> injector.getInstance(MySQLUserManager.Factory.class).create();
            case PSQL -> injector.getInstance(PostgreSQLUserManager.Factory.class).create();
            default -> injector.getInstance(JSONUserManager.class);
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
    public CarbonEventHandler eventHandler() {
        return new CarbonEventHandler();
    }

    @Provides
    @Singleton
    public CarbonMessages carbonMessages(
        final SourcedReceiverResolver receiverResolver,
        final ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
        final UUIDPlaceholderResolver<Audience> uuidPlaceholderResolver,
        final StringPlaceholderResolver<Audience> stringPlaceholderResolver,
        final KeyPlaceholderResolver<Audience> keyPlaceholderResolver,
        final BooleanPlaceholderResolver<Audience> booleanPlaceholderResolver,
        final CarbonMessageSource carbonMessageSource,
        final CarbonMessageSender carbonMessageSender,
        final IMessageRenderer<Audience, String, Component, Component> messageRenderer
    ) throws UnscannableMethodException {
        return Moonshine.<CarbonMessages, Audience>builder(new TypeToken<>() {})
            .receiverLocatorResolver(receiverResolver, 0)
            .sourced(carbonMessageSource)
            .rendered(messageRenderer)
            .sent(carbonMessageSender)
            .resolvingWithStrategy(new StandardPlaceholderResolverStrategyButDifferent<>())
            .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
            .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
            .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Boolean.class, booleanPlaceholderResolver, 0)
            .create(this.getClass().getClassLoader());
    }

    @Provides
    @Singleton
    public ExecutionCoordinatorHolder executionCoordinatorHolder(final Logger logger) {
        return ExecutionCoordinatorHolder.create(logger);
    }

    @Override
    protected void configure() {
        this.install(new FactoryModuleBuilder().build(ArgumentFactory.class));
        this.install(new FactoryModuleBuilder().build(PacketFactory.class));
        this.bind(ChannelRegistry.class).to(CarbonChannelRegistry.class);
    }

}
