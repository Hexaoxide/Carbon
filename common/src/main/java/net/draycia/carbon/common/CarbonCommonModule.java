package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.proximyst.moonshine.Moonshine;
import java.io.IOException;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.BasicChatChannel;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.config.ConfigLoader;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.messages.CarbonMessageParser;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.ServerReceiverResolver;
import net.draycia.carbon.common.messages.UUIDPlaceholderResolver;
import net.draycia.carbon.common.users.JSONUserManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonCommonModule extends AbstractModule {

    @Provides
    @Singleton
    public UserManager userManager(
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
    public ChannelRegistry channelRegistry(
        final PrimaryConfig primaryConfig,
        final Injector injector
    ) {
        // TODO: load channels from config(s)
        // TODO: pick default channel after registration

        return new CarbonChannelRegistry(injector.getInstance(BasicChatChannel.class));
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
        final ServerReceiverResolver serverReceiverResolver,
        final ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
        final UUIDPlaceholderResolver<Audience> uuidPlaceholderResolver,
        final CarbonMessageSource carbonMessageSource,
        final CarbonMessageParser carbonMessageParser,
        final CarbonMessageSender carbonMessageSender
    ) {
        return Moonshine.<Audience>builder()
            .receiver(serverReceiverResolver)
            .placeholder(Component.class, componentPlaceholderResolver)
            .placeholder(UUID.class, uuidPlaceholderResolver)
            .source(carbonMessageSource)
            .parser(carbonMessageParser)
            .sender(carbonMessageSender)
            .create(CarbonMessageService.class, this.getClass().getClassLoader());
    }

}
