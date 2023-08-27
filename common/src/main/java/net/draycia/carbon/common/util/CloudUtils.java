/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.util;

import cloud.commandframework.CommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import com.google.inject.Injector;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.commands.ClearChatCommand;
import net.draycia.carbon.common.command.commands.ContinueCommand;
import net.draycia.carbon.common.command.commands.DebugCommand;
import net.draycia.carbon.common.command.commands.HelpCommand;
import net.draycia.carbon.common.command.commands.IgnoreCommand;
import net.draycia.carbon.common.command.commands.IgnoreListCommand;
import net.draycia.carbon.common.command.commands.JoinCommand;
import net.draycia.carbon.common.command.commands.LeaveCommand;
import net.draycia.carbon.common.command.commands.MuteCommand;
import net.draycia.carbon.common.command.commands.MuteInfoCommand;
import net.draycia.carbon.common.command.commands.NicknameCommand;
import net.draycia.carbon.common.command.commands.ReloadCommand;
import net.draycia.carbon.common.command.commands.ReplyCommand;
import net.draycia.carbon.common.command.commands.ToggleMessagesCommand;
import net.draycia.carbon.common.command.commands.UnignoreCommand;
import net.draycia.carbon.common.command.commands.UnmuteCommand;
import net.draycia.carbon.common.command.commands.UpdateUsernameCommand;
import net.draycia.carbon.common.command.commands.WhisperCommand;
import net.draycia.carbon.common.command.exception.CommandCompleted;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
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

    public static final List<Class<? extends CarbonCommand>> COMMAND_CLASSES = List.of(ClearChatCommand.class,
        ContinueCommand.class, DebugCommand.class, HelpCommand.class, IgnoreCommand.class, MuteCommand.class,
        MuteInfoCommand.class, NicknameCommand.class, ReloadCommand.class, ReplyCommand.class, ToggleMessagesCommand.class,
        UnignoreCommand.class, UnmuteCommand.class, UpdateUsernameCommand.class, WhisperCommand.class, JoinCommand.class, LeaveCommand.class,
        IgnoreListCommand.class);

    private static final List<CarbonCommand> CONSTRUCTED_COMMANDS = new ArrayList<>();

    private CloudUtils() {

    }

    public static void loadCommands(final Injector injector) {
        for (final var commandClass : COMMAND_CLASSES) {
            final CarbonCommand commandInstance = injector.getInstance(commandClass);
            CONSTRUCTED_COMMANDS.add(commandInstance);

            // TODO: load from command-settings.conf
        }
    }

    public static Map<Key, CommandSettings> defaultCommandSettings() {
        final Map<Key, CommandSettings> settings = new HashMap<>();

        for (final var command : CONSTRUCTED_COMMANDS) {
            settings.put(command.key(), command.commandSettings());
        }

        return settings;
    }

    public static void registerCommands(final Map<Key, CommandSettings> settings) {
        for (final var command : CONSTRUCTED_COMMANDS) {
            command.commandSettings(settings.get(command.key()));

            if (command.commandSettings().enabled()) {
                command.init();
            }
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
        final CarbonMessages carbonMessages
    ) {
        commandManager.commandSuggestionProcessor(
            new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<Commander>contains(true).andTrimBeforeLastSpace()
            )
        );

        registerExceptionHandlers(commandManager, carbonMessages);
    }

    public static void registerExceptionHandlers(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        commandManager.registerExceptionHandler(ArgumentParseException.class, (sender, exception) -> {
            final var throwableMessage = CloudUtils.message(exception.getCause());

            carbonMessages.errorCommandArgumentParsing(sender, throwableMessage);
        });
        commandManager.registerExceptionHandler(InvalidCommandSenderException.class, (sender, exception) -> {
            final var senderType = exception.getRequiredSender().getSimpleName();

            carbonMessages.errorCommandInvalidSender(sender, senderType);
        });
        commandManager.registerExceptionHandler(InvalidSyntaxException.class, (sender, exception) -> {
            final var syntax =
                Component.text(exception.getCorrectSyntax()).replaceText(
                    config -> config.match(SPECIAL_CHARACTERS_PATTERN)
                        .replacement(match -> match.color(NamedTextColor.WHITE)));

            carbonMessages.errorCommandInvalidSyntax(sender, syntax);
        });
        commandManager.registerExceptionHandler(NoPermissionException.class, (sender, exception) -> {
            carbonMessages.errorCommandNoPermission(sender);
        });
        commandManager.registerExceptionHandler(CommandExecutionException.class, (sender, exception) -> {
            final Throwable cause = exception.getCause();

            if (cause instanceof CommandCompleted completed) {
                final @Nullable Component msg = completed.componentMessage();
                if (msg != null) {
                    sender.sendMessage(msg);
                }
                return;
            }

            cause.printStackTrace();

            final StringWriter writer = new StringWriter();
            cause.printStackTrace(new PrintWriter(writer));
            final String stackTrace = writer.toString().replaceAll("\t", "    ");
            final @Nullable Component throwableMessage = CloudUtils.message(cause);

            carbonMessages.errorCommandCommandExecution(sender, throwableMessage, stackTrace);
        });
    }

    public static CarbonPlayer nonPlayerMustProvidePlayer(final CarbonMessages messages, final Commander commander) {
        if (commander instanceof PlayerCommander playerCommander) {
            return playerCommander.carbonPlayer();
        }
        throw CommandCompleted.withMessage(messages.commandNeedsPlayer());
    }

}
