package net.draycia.carbon.bukkit;

import com.google.inject.AbstractModule;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.users.MemoryUserManagerBukkit;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkitModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonChatBukkitEntry plugin;
    private final Path dataDirectory;

    CarbonChatBukkitModule(
        final CarbonChatBukkitEntry plugin,
        final Path dataDirectory
    ) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatBukkit.class);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonChatBukkitEntry.class).toInstance(this.plugin);
        this.bind(CarbonServer.class).to(CarbonServerBukkit.class);
        this.bind(UserManager.class).to(MemoryUserManagerBukkit.class);
    }

}
