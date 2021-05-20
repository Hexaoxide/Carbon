package net.draycia.carbon.bukkit;

import com.google.inject.AbstractModule;
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

import java.nio.file.Path;

@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkitModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonChatBukkitEntry plugin;
    private final CarbonChatBukkit carbonChat;
    private final Path dataDirectory;

    CarbonChatBukkitModule(
        final CarbonChatBukkitEntry plugin,
        final CarbonChatBukkit carbonChat,
        final Path dataDirectory
    ) {
        this.plugin = plugin;
        this.carbonChat = carbonChat;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonChatBukkit.class).toInstance(this.carbonChat);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonChatBukkitEntry.class).toInstance(this.plugin);
        this.bind(CarbonServer.class).to(CarbonServerBukkit.class);
        this.bind(UserManager.class).to(MemoryUserManagerBukkit.class);

        this.requestInjection(this.carbonChat);
    }

}
