package net.draycia.carbon.sponge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.sponge.command.SpongeCommander;
import net.draycia.carbon.sponge.command.SpongePlayerCommander;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeModule extends AbstractModule {

    private final CarbonChatSponge carbonChat;
    private final Path configDir;
    private final PluginContainer pluginContainer;

    public CarbonChatSpongeModule(
        final CarbonChatSponge carbonChat,
        final Path configDir,
        final PluginContainer pluginContainer
    ) {
        this.carbonChat = carbonChat;
        this.configDir = configDir;
        this.pluginContainer = pluginContainer;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final SpongeCommandManager<Commander> commandManager = new SpongeCommandManager<>(
            this.pluginContainer,
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            commander -> ((SpongeCommander) commander).commandCause(),
            commandCause -> {
                if (commandCause.subject() instanceof ServerPlayer player) {
                    return new SpongePlayerCommander(this.carbonChat, player, commandCause);
                }

                return SpongeCommander.from(commandCause);
            }
        );

        CloudUtils.decorateCommandManager(commandManager, this.carbonChat.messageService());

        commandManager.parserMapper().cloudNumberSuggestions(true);

        return commandManager;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.configDir);
        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonServer.class).to(CarbonServerSponge.class);
    }

}
