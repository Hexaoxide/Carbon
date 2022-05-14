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
package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.ReceiverResolver;
import net.draycia.carbon.common.messages.StandardPlaceholderResolverStrategyButDifferent;
import net.draycia.carbon.common.messages.placeholders.BooleanPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.UUIDPlaceholderResolver;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.JSONUserManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonCommonModule extends AbstractModule {

    @Provides
    @Singleton
    public UserManager<CarbonPlayerCommon> userManager(
        final ConfigFactory configFactory,
        final Injector injector
    ) {
        switch (Objects.requireNonNull(configFactory.primaryConfig()).storageType()) {
            default -> {
                return injector.getInstance(JSONUserManager.class);
            }
        }
    }

    @Provides
    @Singleton
    public CarbonMessageService messageService(
        final ReceiverResolver receiverResolver,
        final ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
        final UUIDPlaceholderResolver<Audience> uuidPlaceholderResolver,
        final StringPlaceholderResolver<Audience> stringPlaceholderResolver,
        final KeyPlaceholderResolver<Audience> keyPlaceholderResolver,
        final BooleanPlaceholderResolver<Audience> booleanPlaceholderResolver,
        final CarbonMessageSource carbonMessageSource,
        final CarbonMessageSender carbonMessageSender
    ) throws UnscannableMethodException {
        return Moonshine.<CarbonMessageService, Audience>builder(new TypeToken<>() {})
            .receiverLocatorResolver(receiverResolver, 0)
            .sourced(carbonMessageSource)
            .rendered(CarbonChatProvider.carbonChat().messageRenderer())
            .sent(carbonMessageSender)
            .resolvingWithStrategy(new StandardPlaceholderResolverStrategyButDifferent<>())
            .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
            .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
            .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Boolean.class, booleanPlaceholderResolver, 0)
            .create(this.getClass().getClassLoader());
    }

    @Override
    protected void configure() {
        this.bind(ChannelRegistry.class).to(CarbonChannelRegistry.class);
    }

}
