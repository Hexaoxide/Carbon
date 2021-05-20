package net.draycia.carbon.bukkit;

import com.google.inject.AbstractModule;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.users.MemoryUserManagerBukkit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkitModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonChatBukkitEntry plugin;
    private final CarbonChatBukkit cc;

    CarbonChatBukkitModule(final CarbonChatBukkitEntry plugin, final CarbonChatBukkit cc) {
        this.plugin = plugin;
        this.cc = cc;
    }

    @Override
    public void configure() {
        this.bind(CarbonChat.class).toInstance(this.cc);
        this.bind(CarbonChatBukkit.class).toInstance(this.cc);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(CarbonChatBukkitEntry.class).toInstance(this.plugin);
        this.bind(CarbonServer.class).to(CarbonServerBukkit.class);
        this.bind(UserManager.class).to(MemoryUserManagerBukkit.class);

        this.requestInjection(this.cc);
    }

}
