package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import java.io.IOException;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.config.ConfigLoader;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.ReceiverResolver;
import net.draycia.carbon.common.messages.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.UUIDPlaceholderResolver;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.JSONUserManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy;
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonCommonModule extends AbstractModule {

    @Provides
    @Singleton
    public UserManager<CarbonPlayerCommon> userManager(
        final PrimaryConfig primaryConfig,
        final Injector injector
    ) {
        switch (primaryConfig.storageType()) {
            default -> {
                return injector.getInstance(JSONUserManager.class);
            }
        }
    }

    @Provides
    @Singleton
    public PrimaryConfig primaryConfig(final ConfigLoader configLoader) throws IOException {
        final @Nullable PrimaryConfig primaryConfig =
            configLoader.load(PrimaryConfig.class, "config.conf");

        if (primaryConfig == null) {
            throw new IllegalStateException("Primary configuration was unable to load!");
        }

        return primaryConfig;
    }

    @Provides
    @Singleton
    public CarbonMessageService messageService(
        final ReceiverResolver receiverResolver,
        final ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
        final UUIDPlaceholderResolver<Audience> uuidPlaceholderResolver,
        final StringPlaceholderResolver<Audience> stringPlaceholderResolver,
        final KeyPlaceholderResolver<Audience> keyPlaceholderResolver,
        final CarbonMessageSource carbonMessageSource,
        final CarbonMessageSender carbonMessageSender,
        final CarbonMessageRenderer carbonMessageRenderer
    ) throws UnscannableMethodException {
        return Moonshine.<CarbonMessageService, Audience>builder(new TypeToken<>() {})
            .receiverLocatorResolver(receiverResolver, 0)
            .sourced(carbonMessageSource)
            .rendered(carbonMessageRenderer)
            .sent(carbonMessageSender)
            .resolvingWithStrategy(new StandardPlaceholderResolverStrategy<>(new StandardSupertypeThenInterfaceSupertypeStrategy(false)))
            .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
            .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
            .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
            .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
            .create(this.getClass().getClassLoader());
    }

    @Override
    protected void configure() {
        this.bind(ChannelRegistry.class).to(CarbonChannelRegistry.class);
    }

}
