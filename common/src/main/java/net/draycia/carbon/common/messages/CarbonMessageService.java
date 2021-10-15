package net.draycia.carbon.common.messages;

import java.util.UUID;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;

public interface CarbonMessageService {

    @Message("example.command.hello")
    void exampleCommandFeedback(
        final Audience audience,
        final @Placeholder Component plugin
    );

    @Message("unsupported.legacy")
    void unsupportedLegacyChar(
        final Audience audience,
        final @Placeholder String message
    );

    @Message("test.phrase")
    void localeTestMessage(final Audience audience);

    @Message("channel.format.basic")
    Component basicChatFormat(
        final Audience audience,
        final @Placeholder UUID uuid,
        final @Placeholder Component displayname,
        final @Placeholder String username,
        final @Placeholder Component message
    );

    @Message("mute.spy.prefix")
    Component muteSpyPrefix(final Audience audience);

    @Message("channel.change")
    void changedChannels(
        final Audience audience,
        final @Placeholder String channel // TODO: allow MiniMessage based channel "names"
    );

    @Message("whisper.to")
    void whisperSender(
        final SourcedAudience audience,
        final @Placeholder Component senderDisplayName,
        final @Placeholder Component recipientDisplayName,
        final @Placeholder String message
    );

    @Message("whisper.from")
    void whisperRecipient(
        final SourcedAudience audience,
        final @Placeholder Component senderDisplayName,
        final @Placeholder Component recipientDisplayName,
        final @Placeholder String message
    );

    @Message("reply.target.missing")
    void replyTargetNotSet(
        final Audience audience,
        final @Placeholder Component senderDisplayName
    );

    @Message("reply.target.offline")
    void replyTargetOffline(
        final Audience audience,
        final @Placeholder Component senderDisplayName
    );

    @Message("reply.target.self")
    void whisperSelfError(
        final Audience audience,
        final @Placeholder Component senderDisplayName
    );

    @Message("continue.target.missing")
    void whisperTargetNotSet(
        final Audience audience,
        final @Placeholder Component senderDisplayName
    );

    @Message("continue.target.offline")
    void whisperTargetOffline(
        final Audience audience,
        final @Placeholder Component senderDisplayName
    );

    @Message("nickname.set")
    void nicknameSet(
        final Audience audience,
        final @Placeholder Component nickname
    );

    @Message("nickname.set.temporary")
    void temporaryNicknameSet(
        final Audience audience,
        final @Placeholder Component nickname,
        final @Placeholder String duration
    );

    @Message("nickname.set.others")
    void nicknameSetOthers(
        final Audience audience,
        final @Placeholder String target,
        final @Placeholder Component nickname
    );

    @Message("nickname.set.others.temporary")
    void temporaryNicknameSetOthers(
        final Audience audience,
        final @Placeholder String target,
        final @Placeholder Component nickname,
        final @Placeholder String duration
    );

    @Message("nickname.show.others")
    void nicknameShowOthers(
        final Audience audience,
        final @Placeholder String target,
        final @Placeholder Component nickname
    );

    @Message("nickname.show.others.temporary")
    void temporaryNicknameShowOthers(
        final Audience audience,
        final @Placeholder String target,
        final @Placeholder Component nickname,
        final @Placeholder String duration
    );

    @Message("nickname.show.others.unset")
    void nicknameShowOthersUnset(
        final Audience audience,
        final @Placeholder String target
    );

    @Message("nickname.show")
    void nicknameShow(
        final Audience audience,
        final @Placeholder String target,
        final @Placeholder Component nickname
    );

    @Message("nickname.show.temporary")
    void temporaryNicknameShow(
        final Audience audience,
        final @Placeholder String target,
        final @Placeholder Component nickname,
        final @Placeholder String duration
    );

    @Message("nickname.show.unset")
    void nicknameShowUnset(
        final Audience audience,
        final @Placeholder String target
    );

    // pls stop
    @Message("mute.notify.permanent.reason.channel")
    void broadcastPlayerChannelMutedPermanentlyReason(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder String reason,
        final @Placeholder Key channel
    );

    @Message("mute.notify.temporary.reason.channel")
    void broadcastPlayerChannelMutedDurationReason(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder String reason,
        final @Placeholder Key channel,
        final @Placeholder long duration
    );

    @Message("mute.notify.permanent.reason")
    void broadcastPlayerMutedPermanentlyReason(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder String reason
    );

    @Message("mute.notify.temporary.reason")
    void broadcastPlayerMutedDurationReason(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder String reason,
        final @Placeholder long duration
    );

    @Message("mute.notify.permanent.channel")
    void broadcastPlayerChannelMutedPermanently(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder Key channel
    );

    @Message("mute.notify.temporary.channel")
    void broadcastPlayerChannelMutedDuration(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder Key channel,
        final @Placeholder long duration
    );

    @Message("mute.notify.permanent")
    void broadcastPlayerMutedPermanently(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target
    );

    @Message("mute.notify.temporary")
    void broadcastPlayerMutedDuration(
        final Audience audience,
        final @Placeholder String sender,
        final @Placeholder String target,
        final @Placeholder long duration
    );

    @Message("mute.exempt")
    void muteExempt(final Audience audience);

    @Message("mute.notify.player.muted")
    void playerAlertMuted(final Audience audience);

    @Message("mute.notify.player.unmuted")
    void playerAlertUnmuted(final Audience audience);

    @Message("mute.notify.unmuted")
    void broadcastPlayerUnmuted(
        final Audience audience,
        final @Placeholder String target
    );

    @Message("error.command.no_permission")
    Component errorCommandNoPermission();

    @Message("error.command.command_execution")
    Component errorCommandCommandExecution(
        final @Placeholder Component throwableMessage,
        final @Placeholder String stacktrace
    );

    @Message("error.command.argument_parsing")
    Component errorCommandArgumentParsing(final @Placeholder Component throwableMessage);

    @Message("error.command.invalid_sender")
    Component errorCommandInvalidSender(final @Placeholder String senderType);

    @Message("error.command.invalid_syntax")
    Component errorCommandInvalidSyntax(final @Placeholder Component syntax);

}
