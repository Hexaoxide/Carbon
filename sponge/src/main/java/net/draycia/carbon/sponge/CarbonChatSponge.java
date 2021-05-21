package net.draycia.carbon.sponge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.sponge.command.SpongeCommander;
import net.draycia.carbon.sponge.command.SpongePlayerCommander;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Path;

// TODO: this class should die
@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonChatSponge extends CarbonChatCommon {

    private final UserManager userManager;
    private final Logger logger;
    private final CarbonServerSponge carbonServerSponge;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;

    @Inject
    private CarbonChatSponge(
        final PluginContainer pluginContainer,
        final Logger logger,
        final @ForCarbon Path dataDirectory,
        final CarbonServerSponge carbonServerSponge,
        final UserManager userManager,
        final CarbonMessageService messageService
    ) {
        super(messageService);
        this.userManager = userManager;
        this.logger = logger;
        this.carbonServerSponge = carbonServerSponge;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
    }

    public UserManager userManager() {
        return this.userManager;
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public CarbonServer server() {
        return this.carbonServerSponge;
    }

    @Override
    protected CommandManager<Commander> createCommandManager() {
        final SpongeCommandManager<Commander> commandManager = new SpongeCommandManager<>(
            this.pluginContainer,
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            commander -> ((SpongeCommander) commander).commandCause(),
            commandCause -> {
                if (commandCause.subject() instanceof ServerPlayer player) {
                    return new SpongePlayerCommander(this, player, commandCause);
                }
                return SpongeCommander.from(commandCause);
            }
        );
        commandManager.parserMapper().cloudNumberSuggestions(true);
        return commandManager;
    }

}
