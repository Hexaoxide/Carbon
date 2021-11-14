package net.draycia.carbon.common.util;

import cloud.commandframework.CommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import com.google.inject.Injector;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.commands.ClearChatCommand;
import net.draycia.carbon.common.command.commands.ContinueCommand;
import net.draycia.carbon.common.command.commands.DebugCommand;
import net.draycia.carbon.common.command.commands.HelpCommand;
import net.draycia.carbon.common.command.commands.MuteCommand;
import net.draycia.carbon.common.command.commands.NicknameCommand;
import net.draycia.carbon.common.command.commands.ReplyCommand;
import net.draycia.carbon.common.command.commands.UnmuteCommand;
import net.draycia.carbon.common.command.commands.WhisperCommand;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CloudUtils {

    private static final Component NULL = Component.text("null");
    private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

    public static final List<Class<?>> COMMAND_CLASSES = List.of(ClearChatCommand.class, ContinueCommand.class,
        DebugCommand.class, HelpCommand.class, MuteCommand.class, NicknameCommand.class, ReplyCommand.class,
        UnmuteCommand.class, WhisperCommand.class);

    private CloudUtils() {

    }

    public static void registerCommands(final Injector injector) {
        for (final var commandClass : COMMAND_CLASSES) {
            injector.getInstance(commandClass);
        }
    }

    public static Component message(final Throwable throwable) {
        final @Nullable Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

    public static String rawInputByMatchingName(
        final LinkedList<String> rawInput,
        final CarbonPlayer recipient
    ) {
        return rawInput
            .stream()
            .filter(it -> it.equalsIgnoreCase(recipient.username()))
            .findFirst()
            .orElse(recipient.username());
    }

    public static void decorateCommandManager(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService
    ) {
        registerExceptionHandlers(commandManager, messageService);
    }

    public static void registerExceptionHandlers(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService
    ) {
        commandManager.registerExceptionHandler(ArgumentParseException.class, (sender, exception) -> {
            final var throwableMessage = CloudUtils.message(exception.getCause());

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
            final @Nullable Component throwableMessage = CloudUtils.message(cause);

            messageService.errorCommandCommandExecution(sender, throwableMessage, stackTrace);
        });
    }

}
