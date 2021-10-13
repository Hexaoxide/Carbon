package net.draycia.carbon.bukkit;

import cloud.commandframework.CommandManager;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
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

        // Super jank solution
        // These will slowly be converted to MiniMessage in the locale if possible
        new MinecraftExceptionHandler<Commander>()
            .withHandler(MinecraftExceptionHandler.ExceptionType.ARGUMENT_PARSING, exception -> {
                final var throwableMessage = ComponentMessageThrowable.getMessage(exception.getCause());

                return this.carbonChat.messageService().errorCommandArgumentParsing(throwableMessage);
            })
            .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SENDER, exception -> {
                final var senderType = ((InvalidCommandSenderException) exception)
                    .getRequiredSender().getSimpleName();

                return this.carbonChat.messageService().errorCommandInvalidSender(senderType);
            })
            .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, exception -> {
                final var syntax = this.highlight(
                    Component.text(
                        String.format("/%s", ((InvalidSyntaxException) exception).getCorrectSyntax()),
                        NamedTextColor.GRAY
                    )
                );

                return this.carbonChat.messageService().errorCommandInvalidSyntax(syntax);
            })
            .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, exception -> {
                return this.carbonChat.messageService().errorCommandNoPermission();
            })
            .withHandler(MinecraftExceptionHandler.ExceptionType.COMMAND_EXECUTION, exception -> {
                final Throwable cause = exception.getCause();
                cause.printStackTrace();

                final StringWriter writer = new StringWriter();
                cause.printStackTrace(new PrintWriter(writer));
                final String stackTrace = writer.toString().replaceAll("\t", "    ");
                final @Nullable Component throwableMessage = ComponentMessageThrowable.getMessage(cause);

                return this.carbonChat.messageService().errorCommandCommandExecution(throwableMessage, stackTrace);
            })
            .apply(commandManager, commander -> commander);

        commandManager.registerAsynchronousCompletions();
        commandManager.registerBrigadier();

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
        this.bind(CarbonChatBukkit.class).toInstance(this.carbonChat);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerBukkit.class);
    }

    private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

    private Component highlight(final @NonNull Component component) {
        return component.replaceText(config -> {
            config.match(SPECIAL_CHARACTERS_PATTERN);
            config.replacement(match -> match.color(NamedTextColor.WHITE));
        });
    }

}
