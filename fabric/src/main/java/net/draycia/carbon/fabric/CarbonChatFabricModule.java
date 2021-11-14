package net.draycia.carbon.fabric;

import cloud.commandframework.CommandManager;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
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
import net.draycia.carbon.fabric.command.FabricCommander;
import net.draycia.carbon.fabric.command.FabricPlayerCommander;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabricModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonChatFabric carbonChat;
    private final Path dataDirectory;

    CarbonChatFabricModule(
        final CarbonChatFabric carbonChat,
        final Path dataDirectory
    ) {
        this.carbonChat = carbonChat;
        this.dataDirectory = dataDirectory;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final FabricServerCommandManager<Commander> commandManager;

        try {
            commandManager = new FabricServerCommandManager<>(
                AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
                commandSource -> {
                    if (commandSource.getEntity() instanceof Player player) {
                        return new FabricPlayerCommander(this.carbonChat.minecraftServer(), this.carbonChat, player);
                    }
                    return FabricCommander.from(this.carbonChat.minecraftServer(), commandSource);
                },
                commander -> ((FabricCommander) commander).commandSourceStack()
            );
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to initialize command manager.", ex);
        }

        CloudUtils.decorateCommandManager(commandManager, this.carbonChat.messageService());

        final @Nullable CloudBrigadierManager<Commander, ?> brigadierManager =
            commandManager.brigadierManager();

        if (brigadierManager != null) {
            brigadierManager.setNativeNumberSuggestions(false);
        }

        return commandManager;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonChatFabric.class).toInstance(this.carbonChat);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerFabric.class);
    }

}
