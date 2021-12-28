/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.messages;

import java.util.UUID;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.util.ChatType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;

public interface CarbonMessages {

    /*
     * =============================================================
     * ======================== Basic Chat =========================
     * =============================================================
     */

    @Message("channel.format.basic")
    @ChatType(MessageType.CHAT)
    RenderedMessage basicChatFormat(
        final Audience audience,
        final UUID uuid,
        @Placeholder("display_name") final Component displayName,
        final String username,
        final Component message
    );

    @Message("channel.change")
    void changedChannels(final Audience audience, final String channel);

    /*
     * =============================================================
     * =========================== Mutes ===========================
     * =============================================================
     */

    @Message("mute.info.self.muted")
    void muteInfoSelfMuted(final Audience audience);

    @Message("mute.info.self.not_muted")
    void muteInfoSelfNotMuted(final Audience audience);

    @Message("mute.info.not_muted")
    void muteInfoNotMuted(final Audience audience, final Component target);

    @Message("mute.info.muted")
    void muteInfoMuted(final Audience audience, final Component target, final boolean muted);

    @Message("mute.unmute.alert.target")
    void unmuteAlertRecipient(final Audience audience);

    @Message("mute.unmute.alert.players")
    void unmuteAlertPlayers(final Audience audience, final Component target);

    @Message("mute.unmute.no_target")
    void unmuteNoTarget(final Audience audience);

    @Message("mute.exempt")
    void muteExempt(final Audience audience);

    @Message("mute.alert.target")
    void muteAlertRecipient(final Audience audience);

    @Message("mute.alert.players")
    void muteAlertPlayers(final Audience audience, final Component target);

    @Message("mute.cannot_speak")
    void muteCannotSpeak(final Audience audience);

    @Message("mute.no_target")
    void muteNoTarget(final Audience audience);

    @Message("mute.spy.prefix")
    RenderedMessage muteSpyPrefix(final Audience audience);

    /*
     * =============================================================
     * ====================== Direct Messages ======================
     * =============================================================
     */

    @Message("whisper.to")
    @ChatType(MessageType.CHAT)
    void whisperSender(
        final SourcedAudience audience,
        @Placeholder("sender_display_name") final Component senderDisplayName,
        @Placeholder("recipient_display_name") final Component recipientDisplayName,
        final String message
    );

    @Message("whisper.from")
    @ChatType(MessageType.CHAT)
    void whisperRecipient(
        final SourcedAudience audience,
        @Placeholder("sender_display_name") final Component senderDisplayName,
        @Placeholder("recipient_display_name") final Component recipientDisplayName,
        final String message
    );

    @Message("whisper.console")
    void whisperConsoleLog(
        final Audience audience,
        @Placeholder("sender_display_name") final Component senderDisplayName,
        @Placeholder("recipient_display_name") final Component recipientDisplayName,
        final String message
    );

    @Message("reply.target.missing")
    void replyTargetNotSet(final Audience audience, @Placeholder("sender_display_name") final Component senderDisplayName);

    @Message("reply.target.self")
    void whisperSelfError(final Audience audience, @Placeholder("sender_display_name") final Component senderDisplayName);

    @Message("whisper.continue.target_missing")
    void whisperTargetNotSet(
        final Audience audience,
        @Placeholder("sender_display_name") final Component senderDisplayName
    );

    @Message("whisper.ignoring_target")
    void whisperIgnoringTarget(final Audience audience, final Component target);

    @Message("whisper.ignored_by_target")
    void whisperTargetIgnoring(final Audience audience, final Component target);

    /*
     * =============================================================
     * ========================= Nicknames =========================
     * =============================================================
     */

    @Message("nickname.set")
    void nicknameSet(final Audience audience, final Component nickname);

    @Message("nickname.set.others")
    void nicknameSetOthers(final Audience audience, final String target, final Component nickname);

    @Message("nickname.show.others")
    void nicknameShowOthers(final Audience audience, final String target, final Component nickname);

    @Message("nickname.show.others.unset")
    void nicknameShowOthersUnset(final Audience audience, final String target);

    @Message("nickname.show")
    void nicknameShow(final Audience audience, final String target, final Component nickname);

    @Message("nickname.show.unset")
    void nicknameShowUnset(final Audience audience, final String target);

    @Message("nickname.reset")
    void nicknameReset(final Audience audience);

    @Message("nickname.reset.others")
    void nicknameResetOthers(final Audience audience, final String target);

    @Message("nickname.set.self.error")
    void nicknameCannotSetOwn(final Audience audience);

    @Message("nickname.see.self.error")
    void nicknameCannotSeeOwn(final Audience audience);

    /*
     * =============================================================
     * ========================== Ignore ===========================
     * =============================================================
     */

    @Message("ignore.exempt")
    void ignoreExempt(final Audience audience, final Component target);

    @Message("ignore.now_ignoring")
    void nowIgnoring(final Audience audience, final Component target);

    @Message("ignore.no_longer_ignoring")
    void noLongerIgnoring(final Audience audience, final Component target);

    @Message("ignore.invalid_target")
    void ignoreTargetInvalid(final Audience audience);

    /*
     * =============================================================
     * ========================== Reload ===========================
     * =============================================================
     */

    @Message("config.reload.success")
    void configReloaded(final Audience audience);

    @Message("config.reload.failed")
    void configReloadFailed(final Audience audience);

    /*
     * =============================================================
     * ====================== Cloud Messages =======================
     * =============================================================
     */

    @Message("error.command.no_permission")
    void errorCommandNoPermission(final Audience audience);

    @Message("error.command.command_execution")
    void errorCommandCommandExecution(
        final Audience audience,
        @Placeholder("throwable_message") final Component throwableMessage,
        final String stacktrace
    );

    @Message("error.command.argument_parsing")
    void errorCommandArgumentParsing(final Audience audience, @Placeholder("throwable_message") final Component throwableMessage);

    @Message("error.command.invalid_player")
    RenderedMessage errorCommandInvalidPlayer(final Audience audience, final String input);

    @Message("error.command.invalid_sender")
    void errorCommandInvalidSender(final Audience audience, final String sender_type);

    @Message("error.command.invalid_syntax")
    void errorCommandInvalidSyntax(final Audience audience, final Component syntax);

    /*
     * =============================================================
     * =================== Command Documentation ===================
     * =============================================================
     */

    @Message("command.clearchat.description")
    RenderedMessage commandClearChatDescription();

    @Message("command.continue.argument.message")
    RenderedMessage commandContinueArgumentMessage();

    @Message("command.continue.description")
    RenderedMessage commandContinueDescription();

    @Message("command.debug.argument.player")
    RenderedMessage commandDebugArgumentPlayer();

    @Message("command.debug.description")
    RenderedMessage commandDebugDescription();

    @Message("command.help.argument.query")
    RenderedMessage commandHelpArgumentQuery();

    @Message("command.help.description")
    RenderedMessage commandHelpDescription();

    @Message("command.ignore.argument.player")
    RenderedMessage commandIgnoreArgumentPlayer();

    @Message("command.ignore.argument.uuid")
    RenderedMessage commandIgnoreArgumentUUID();

    @Message("command.ignore.description")
    RenderedMessage commandIgnoreDescription();

    @Message("command.mute.argument.player")
    RenderedMessage commandMuteArgumentPlayer();

    @Message("command.mute.argument.uuid")
    RenderedMessage commandMuteArgumentUUID();

    @Message("command.mute.description")
    RenderedMessage commandMuteDescription();

    @Message("command.muteinfo.argument.player")
    RenderedMessage commandMuteInfoArgumentPlayer();

    @Message("command.muteinfo.argument.uuid")
    RenderedMessage commandMuteInfoArgumentUUID();

    @Message("command.muteinfo.description")
    RenderedMessage commandMuteInfoDescription();

    @Message("command.nickname.argument.player")
    RenderedMessage commandNicknameArgumentPlayer();

    @Message("command.nickname.argument.nickname")
    RenderedMessage commandNicknameArgumentNickname();

    @Message("command.nickname.argument.reset")
    RenderedMessage commandNicknameArgumentReset();

    @Message("command.nickname.description")
    RenderedMessage commandNicknameDescription();

    @Message("command.reload.description")
    RenderedMessage commandReloadDescription();

    @Message("command.reply.argument.message")
    RenderedMessage commandReplyArgumentMessage();

    @Message("command.reply.description")
    RenderedMessage commandReplyDescription();

    @Message("command.unignore.argument.player")
    RenderedMessage commandUnignoreArgumentPlayer();

    @Message("command.unignore.argument.uuid")
    RenderedMessage commandUnignoreArgumentUUID();

    @Message("command.unignore.description")
    RenderedMessage commandUnignoreDescription();

    @Message("command.unmute.argument.player")
    RenderedMessage commandUnmuteArgumentPlayer();

    @Message("command.unmute.argument.uuid")
    RenderedMessage commandUnmuteArgumentUUID();

    @Message("command.unmute.description")
    RenderedMessage commandUnmuteDescription();

    @Message("command.whisper.argument.player")
    RenderedMessage commandWhisperArgumentPlayer();

    @Message("command.whisper.argument.message")
    RenderedMessage commandWhisperArgumentMessage();

    @Message("command.whisper.description")
    RenderedMessage commandWhisperDescription();

}
