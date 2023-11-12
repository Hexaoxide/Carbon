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
package net.draycia.carbon.common.command.commands;

import com.google.inject.Inject;
import java.util.UUID;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

@DefaultQualifier(NonNull.class)
public final class ReplyCommand extends CarbonCommand {

    private final UserManager<?> users;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages messages;
    private final WhisperCommand.WhisperHandler whisper;

    @Inject
    public ReplyCommand(
        final UserManager<?> userManager,
        final CommandManager<Commander> commandManager,
        final CarbonMessages messages,
        final WhisperCommand.WhisperHandler whisper
    ) {
        this.users = userManager;
        this.commandManager = commandManager;
        this.messages = messages;
        this.whisper = whisper;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("reply", "r");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "reply");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .required("message", greedyStringParser(), richDescription(this.messages.commandReplyArgumentMessage()))
            .permission("carbon.whisper.reply")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.messages.commandReplyDescription()))
            .handler(ctx -> {
                final CarbonPlayer sender = ctx.sender().carbonPlayer();

                if (sender.muted()) {
                    this.messages.muteCannotSpeak(sender);
                    return;
                }

                final String message = ctx.get("message");
                final @Nullable UUID replyTarget = sender.whisperReplyTarget();

                if (replyTarget == null) {
                    this.messages.replyTargetNotSet(sender, sender.displayName());
                    return;
                }

                final @MonotonicNonNull CarbonPlayer recipient = this.users.user(replyTarget).join();

                this.whisper.whisper(sender, recipient, message);
            })
            .build();

        this.commandManager.command(command);
    }

}
