package net.draycia.carbon.sponge;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.sponge.users.MemoryUserManagerSponge;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;

public class CarbonChatSpongeModule extends AbstractModule {

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(Path.class).annotatedWith(ForCarbon.class).toProvider(new Provider<>() {
            @Inject @ConfigDir(sharedRoot = false) Path path;

            @Override
            public Path get() {
                return this.path;
            }
        });
        this.bind(CarbonChat.class).to(CarbonChatSponge.class);
        this.bind(CarbonChatSponge.class);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
        this.bind(UserManager.class).to(MemoryUserManagerSponge.class);
    }

}
