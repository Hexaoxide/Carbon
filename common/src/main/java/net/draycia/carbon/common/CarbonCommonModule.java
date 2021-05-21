package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.proximyst.moonshine.Moonshine;
import net.draycia.carbon.common.messages.CarbonMessageParser;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.ServerReceiverResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CarbonCommonModule extends AbstractModule {

    @Provides
    @Singleton
    public CarbonMessageService messageService(
        final @NonNull ServerReceiverResolver serverReceiverResolver,
        final @NonNull ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
        final @NonNull CarbonMessageSource carbonMessageSource,
        final @NonNull CarbonMessageParser carbonMessageParser,
        final @NonNull CarbonMessageSender carbonMessageSender
    ) {
        return Moonshine.<Audience>builder()
            .receiver(serverReceiverResolver)
            .placeholder(Component.class, componentPlaceholderResolver)
            .source(carbonMessageSource)
            .parser(carbonMessageParser)
            .sender(carbonMessageSender)
            .create(CarbonMessageService.class, this.getClass().getClassLoader());
    }

}
