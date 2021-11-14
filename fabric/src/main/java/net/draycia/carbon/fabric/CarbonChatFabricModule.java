package net.draycia.carbon.fabric;

import cloud.commandframework.CommandManager;
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
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabricModule extends AbstractModule {

    private final Logger logger;
    private final CarbonChatFabric carbonChat;
    private final Path dataDirectory;

    CarbonChatFabricModule(
        final CarbonChatFabric carbonChat,
        final Logger logger,
        final Path dataDirectory
    ) {
        this.logger = logger;
        this.carbonChat = carbonChat;
        this.dataDirectory = dataDirectory;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final FabricServerCommandManager<Commander> commandManager = new FabricServerCommandManager<>(
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            commandSourceStack -> {
                if (commandSourceStack.getEntity() instanceof ServerPlayer) {
                    return new FabricPlayerCommander(this.carbonChat, commandSourceStack);
                }
                return FabricCommander.from(commandSourceStack);
            },
            commander -> ((FabricCommander) commander).commandSourceStack()
        );

        CloudUtils.decorateCommandManager(commandManager, this.carbonChat.messageService());

        commandManager.brigadierManager().setNativeNumberSuggestions(false);

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
