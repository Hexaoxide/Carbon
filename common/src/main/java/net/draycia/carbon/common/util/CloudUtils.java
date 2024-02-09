/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.exception.CommandCompleted;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.util.TypeUtils;

@DefaultQualifier(NonNull.class)
public final class CloudUtils {

    private static final Component NULL = Component.text("null");
    private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

    @Inject
    private static Provider<Set<CarbonCommand>> commands;

    private CloudUtils() {

    }

    public static Map<Key, CommandSettings> defaultCommandSettings() {
        final Map<Key, CommandSettings> settings = new HashMap<>();

        for (final var command : commands.get()) {
            settings.put(command.key(), command.defaultCommandSettings());
        }

        return settings;
    }

    public static void registerCommands(final Set<CarbonCommand> commands, final Map<Key, CommandSettings> settings) {
        for (final var command : commands) {
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

    public static void decorateCommandManager(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        registerExceptionHandlers(commandManager, carbonMessages);
    }

    public static void registerExceptionHandlers(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        commandManager.exceptionController().registerHandler(ArgumentParseException.class, ctx ->
            carbonMessages.errorCommandArgumentParsing(ctx.context().sender(), CloudUtils.message(ctx.exception().getCause()))
        ).registerHandler(InvalidCommandSenderException.class, ctx ->
            carbonMessages.errorCommandInvalidSender(ctx.context().sender(), TypeUtils.simpleName(ctx.exception().requiredSender()))
        ).registerHandler(InvalidSyntaxException.class, ctx ->
            carbonMessages.errorCommandInvalidSyntax(ctx.context().sender(), Component.text(ctx.exception().correctSyntax()).replaceText(
                config -> config.match(SPECIAL_CHARACTERS_PATTERN)
                    .replacement(match -> match.color(NamedTextColor.WHITE))))
        ).registerHandler(NoPermissionException.class, ctx ->
            carbonMessages.errorCommandNoPermission(ctx.context().sender())
        ).registerHandler(CommandExecutionException.class, ctx -> {
            final Throwable cause = ctx.exception().getCause();

            if (cause instanceof CommandCompleted completed) {
                final @Nullable Component msg = completed.componentMessage();
                if (msg != null) {
                    ctx.context().sender().sendMessage(msg);
                }
                return;
            }

            cause.printStackTrace();

            final StringWriter writer = new StringWriter();
            cause.printStackTrace(new PrintWriter(writer));
            final String stackTrace = writer.toString().replaceAll("\t", "    ");
            final @Nullable Component throwableMessage = CloudUtils.message(cause);

            carbonMessages.errorCommandCommandExecution(ctx.context().sender(), throwableMessage, stackTrace);
        });
    }

    public static CarbonPlayer nonPlayerMustProvidePlayer(final CarbonMessages messages, final Commander commander) {
        if (commander instanceof PlayerCommander playerCommander) {
            return playerCommander.carbonPlayer();
        }
        throw CommandCompleted.withMessage(messages.commandNeedsPlayer());
    }

}
