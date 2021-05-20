package net.draycia.carbon.sponge;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.proximyst.moonshine.Moonshine;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.messages.CarbonMessageParser;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.ServerReceiverResolver;
import net.draycia.carbon.sponge.users.MemoryUserManagerSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;

public class CarbonChatSpongeModule extends AbstractModule {

    private final Logger logger;
    private final CarbonChatSponge carbonChatSponge;
    private final CarbonChatSpongeEntry carbonChatSpongeEntry;
    private final Path dataDirectory;

    public CarbonChatSpongeModule(
        Logger logger,
        CarbonChatSponge carbonChatSponge,
        CarbonChatSpongeEntry carbonChatSpongeEntry,
        Path dataDirectory
    ) {
        this.logger = logger;
        this.carbonChatSponge = carbonChatSponge;
        this.carbonChatSpongeEntry = carbonChatSpongeEntry;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public void configure() {
        this.bind(CarbonChat.class).toInstance(this.carbonChatSponge);
        this.bind(CarbonChatSponge.class).toInstance(this.carbonChatSponge);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).toInstance(this.dataDirectory);
        this.bind(CarbonChatSpongeEntry.class).toInstance(this.carbonChatSpongeEntry);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
        this.bind(UserManager.class).to(MemoryUserManagerSponge.class);

        this.requestInjection(this.carbonChatSponge);
    }

    // TODO: this is shared between both, abstract into common maybe?
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
