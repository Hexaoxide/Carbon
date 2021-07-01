package net.draycia.carbon.velocity;

import com.google.inject.AbstractModule;
import java.net.URISyntaxException;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatVelocityModule extends AbstractModule {

    private final Logger logger;
    private final Path dataDirectory;

    CarbonChatVelocityModule(
        final Logger logger,
        final Path dataDirectory
    ) throws URISyntaxException {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatVelocity.class);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerVelocity.class);
    }

}
