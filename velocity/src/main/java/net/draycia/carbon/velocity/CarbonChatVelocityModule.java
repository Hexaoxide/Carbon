package net.draycia.carbon.velocity;

import com.google.inject.AbstractModule;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.CarbonJar;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.velocity.users.MemoryUserManagerVelocity;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatVelocityModule extends AbstractModule {

    private final Logger logger;
    private final Path dataDirectory;
    private final Path pluginJar;

    CarbonChatVelocityModule(
        final Logger logger,
        final Path dataDirectory
    ) throws URISyntaxException {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.pluginJar = Paths.get(CarbonChatVelocityEntry.class
            .getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatVelocity.class);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(Path.class).annotatedWith(CarbonJar.class).toInstance(this.pluginJar);
        this.bind(CarbonServer.class).to(CarbonServerVelocity.class);
        this.bind(UserManager.class).to(MemoryUserManagerVelocity.class);
    }

}
