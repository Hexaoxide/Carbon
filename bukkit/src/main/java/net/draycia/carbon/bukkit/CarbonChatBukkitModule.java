package net.draycia.carbon.bukkit;

import cloud.commandframework.CommandManager;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.regex.Pattern;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.bukkit.command.BukkitCommander;
import net.draycia.carbon.bukkit.command.BukkitPlayerCommander;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkitModule extends AbstractModule {

    private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");
    private static final Component NULL = Component.text("null");

    private final Logger logger = LogManager.getLogger("CarbonChat");
    private final CarbonChatBukkit carbonChat;
    private final Path dataDirectory;

    CarbonChatBukkitModule(
        final CarbonChatBukkit carbonChat,
        final Path dataDirectory
    ) {
        this.carbonChat = carbonChat;
        this.dataDirectory = dataDirectory;
    }

    @Provides
    @Singleton
    public CommandManager<Commander> commandManager() {
        final PaperCommandManager<Commander> commandManager;

        try {
            commandManager = new PaperCommandManager<>(
                this.carbonChat,
                AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
                commandSender -> {
                    if (commandSender instanceof Player player) {
                        return new BukkitPlayerCommander(this.carbonChat, player);
                    }
                    return BukkitCommander.from(commandSender);
                },
                commander -> ((BukkitCommander) commander).commandSender()
            );
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to initialize command manager.", ex);
        }

        decorateCommandManager(commandManager, this.carbonChat.messageService());

        commandManager.registerAsynchronousCompletions();
        commandManager.registerBrigadier();

        final @Nullable CloudBrigadierManager<Commander, ?> brigadierManager =
            commandManager.brigadierManager();

        if (brigadierManager != null) {
            brigadierManager.setNativeNumberSuggestions(false);
        }

        return commandManager;
    }

    // This should be in common and applied to every platforms command manager, not in bukkit module
    private static void decorateCommandManager(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService
    ) {
        registerExceptionHandlers(commandManager, messageService);
    }

    private static void registerExceptionHandlers(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService
    ) {
        commandManager.registerExceptionHandler(ArgumentParseException.class, (sender, exception) -> {
            final var throwableMessage = message(exception.getCause());

            messageService.errorCommandArgumentParsing(sender, throwableMessage);
        });
        commandManager.registerExceptionHandler(InvalidCommandSenderException.class, (sender, exception) -> {
            final var senderType = exception.getRequiredSender().getSimpleName();

            messageService.errorCommandInvalidSender(sender, senderType);
        });
        commandManager.registerExceptionHandler(InvalidSyntaxException.class, (sender, exception) -> {
            final var syntax =
                Component.text(exception.getCorrectSyntax()).replaceText(
                    config -> config.match(SPECIAL_CHARACTERS_PATTERN)
                        .replacement(match -> match.color(NamedTextColor.WHITE)));

            messageService.errorCommandInvalidSyntax(sender, syntax);
        });
        commandManager.registerExceptionHandler(NoPermissionException.class, (sender, exception) -> {
            messageService.errorCommandNoPermission(sender);
        });
        commandManager.registerExceptionHandler(CommandExecutionException.class, (sender, exception) -> {
            final Throwable cause = exception.getCause();
            cause.printStackTrace();

            final StringWriter writer = new StringWriter();
            cause.printStackTrace(new PrintWriter(writer));
            final String stackTrace = writer.toString().replaceAll("\t", "    ");
            final @Nullable Component throwableMessage = message(cause);

            messageService.errorCommandCommandExecution(sender, throwableMessage, stackTrace);
        });
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).toInstance(this.carbonChat);
        this.bind(CarbonChatBukkit.class).toInstance(this.carbonChat);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerBukkit.class);
    }

    private static Component message(final Throwable throwable) {
        final @Nullable Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

}
