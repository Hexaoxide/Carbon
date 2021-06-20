package net.draycia.carbon.sponge;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.CarbonJar;
import net.draycia.carbon.common.ForCarbon;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.plugin.PluginContainer;

@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeModule extends AbstractModule {

    private final PluginContainer pluginContainer;
    private final Path configDir;

    @Inject
    private CarbonChatSpongeModule(
        final PluginContainer pluginContainer,
        @ConfigDir(sharedRoot = false) final Path configDir
    ) {
        this.pluginContainer = pluginContainer;
        this.configDir = configDir;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.configDir);
        this.bind(Path.class).annotatedWith(CarbonJar.class).toInstance(this.pluginContainer.path());
        this.bind(CarbonChat.class).to(CarbonChatSponge.class);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
    }

}
