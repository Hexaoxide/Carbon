package net.draycia.carbon.sponge;

import com.google.inject.AbstractModule;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.CarbonJar;
import net.draycia.carbon.common.ForCarbon;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.plugin.PluginContainer;

@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeModule extends AbstractModule {

    private final CarbonChatSponge carbonChatSponge;
    private final Path configDir;
    private final PluginContainer pluginContainer;

    public CarbonChatSpongeModule(
        final CarbonChatSponge carbonChatSponge,
        final Path configDir,
        final PluginContainer pluginContainer
    ) {
        this.carbonChatSponge = carbonChatSponge;
        this.configDir = configDir;
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.configDir);
        this.bind(Path.class).annotatedWith(CarbonJar.class).toInstance(this.pluginContainer.path());
        this.bind(CarbonChat.class).toInstance(this.carbonChatSponge);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
    }

}
