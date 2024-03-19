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
package net.draycia.carbon.common.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;

public interface CarbonMessages {

    /*
     * =============================================================
     * ======================== Basic Chat =========================
     * =============================================================
     */

    @Message("channel.change")
    void changedChannels(final Audience audience, final String channel);

    @Message("channel.radius.empty_recipients")
    void emptyRecipients(final Audience audience);

    @Message("channel.radius.spy")
    void radiusSpy(Audience audience, Component message);

    @Message("channel.not_found")
    void channelNotFound(final Audience audience);

    @Message("channel.not_left")
    void channelNotLeft(final Audience audience);

    @Message("channel.already_left")
    void channelAlreadyLeft(final Audience audience);

    @Message("channel.no_permission")
    Component channelNoPermission(Audience audience);

    @Message("channel.left")
    void channelLeft(final Audience audience);

    @Message("channel.joined")
    void channelJoined(final Audience audience);

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
    Component muteSpyPrefix(final Audience audience);

    /*
     * =============================================================
     * ====================== Direct Messages ======================
     * =============================================================
     */

    @Message("whisper.to")
    Component whisperSender(
        @NotPlaceholder SourcedAudience audience,
        String senderUsername,
        Component senderDisplayName,
        String recipientUsername,
        Component recipientDisplayName,
        Component message
    );

    @Message("whisper.from")
    Component whisperRecipient(
        @NotPlaceholder SourcedAudience audience,
        String senderUsername,
        Component senderDisplayName,
        String recipientUsername,
        Component recipientDisplayName,
        Component message
    );

    @Message("whisper.from.spy")
    void whisperRecipientSpy(
        Audience audience,
        String senderUsername,
        Component senderDisplayName,
        String recipientUsername,
        Component recipientDisplayName,
        Component message
    );

    @Message("whisper.console")
    void whisperConsoleLog(
        Audience audience,
        String senderUsername,
        Component senderDisplayName,
        String recipientUsername,
        Component recipientDisplayName,
        Component message
    );

    @Message("whisper.error")
    void whisperError(
        final Audience audience,
        @Placeholder("sender_display_name") final Component senderDisplayName,
        @Placeholder("recipient_display_name") final Component recipientDisplayName
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

    @Message("whisper.ignoring_all")
    void whisperIgnoringAll(final Audience audience);

    @Message("whisper.ignoring_target")
    void whisperIgnoringTarget(final Audience audience, final Component target);

    @Message("whisper.ignored_by_target")
    void whisperTargetIgnoring(final Audience audience, final Component target);

    @Message("whisper.ignored_dms")
    void whisperTargetIgnoringDMs(final Audience audience, final Component target);

    @Message("whisper.toggled.on")
    void whispersToggledOn(final Audience audience);

    @Message("whisper.toggled.off")
    void whispersToggledOff(final Audience audience);

    /*
     * =============================================================
     * ========================= Nicknames =========================
     * =============================================================
     */

    @Message("nickname.set")
    void nicknameSet(final Audience audience, final Component nickname);

    @Message("nickname.set.others")
    void nicknameSetOthers(final Audience audience, final String target, final Component nickname);

    @Message("nickname.error.character_limit")
    void nicknameErrorCharacterLimit(
        final Audience audience,
        final Component nickname,
        final int minLength,
        final int maxLength
    );

    @Message("nickname.error.blacklist")
    void nicknameErrorBlackList(final Audience audience, final Component nickname);

    @Message("nickname.error.filter")
    void nicknameErrorFilter(final Audience audience, final Component nickname);

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

    /*
     * =============================================================
     * ========================== Ignore ===========================
     * =============================================================
     */

    @Message("ignore.already_ignored")
    void alreadyIgnored(final Audience audience, final Component target);

    @Message("ignore.not_ignored")
    void notIgnored(Audience audience, Component target);

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
     * ========================== Spying ===========================
     * =============================================================
     */

    @Message("command.spy.enabled")
    void commandSpyEnabled(final Audience audience);

    @Message("command.spy.disabled")
    void commandSpyDisabled(final Audience audience);

    @Message("command.spy.description")
    Component commandSpyDescription();

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
    Component errorCommandInvalidPlayer(String input);

    @Message("error.command.invalid_sender")
    void errorCommandInvalidSender(final Audience audience, final String sender_type);

    @Message("error.command.invalid_syntax")
    void errorCommandInvalidSyntax(final Audience audience, final Component syntax);

    @Message("error.command.command_needs_player")
    Component commandNeedsPlayer();

    /*
     * =============================================================
     * =================== Command Documentation ===================
     * =============================================================
     */

    @Message("command.clearchat.description")
    Component commandClearChatDescription();

    @Message("command.continue.argument.message")
    Component commandContinueArgumentMessage();

    @Message("command.continue.description")
    Component commandContinueDescription();

    @Message("command.debug.argument.player")
    Component commandDebugArgumentPlayer();

    @Message("command.debug.description")
    Component commandDebugDescription();

    @Message("command.help.argument.query")
    Component commandHelpArgumentQuery();

    @Message("command.help.description")
    Component commandHelpDescription();

    @Message("command.ignore.argument.player")
    Component commandIgnoreArgumentPlayer();

    @Message("command.ignore.argument.uuid")
    Component commandIgnoreArgumentUUID();

    @Message("command.ignore.description")
    Component commandIgnoreDescription();

    @Message("command.ignorelist.description")
    Component commandIgnoreListDescription();

    @Message("command.ignorelist.none_ignored")
    void commandIgnoreListNoneIgnored(Audience audience);

    @Message("command.ignorelist.pagination_header")
    Component commandIgnoreListPaginationHeader(int page, int pages);

    @Message("command.ignorelist.pagination_element")
    Component commandIgnoreListPaginationElement(Component displayName, String username);

    @Message("command.join.description")
    Component commandJoinDescription();

    @Message("command.leave.description")
    Component commandLeaveDescription();

    @Message("command.mute.argument.player")
    Component commandMuteArgumentPlayer();

    @Message("command.mute.argument.uuid")
    Component commandMuteArgumentUUID();

    @Message("command.mute.description")
    Component commandMuteDescription();

    @Message("command.muteinfo.argument.player")
    Component commandMuteInfoArgumentPlayer();

    @Message("command.muteinfo.argument.uuid")
    Component commandMuteInfoArgumentUUID();

    @Message("command.muteinfo.description")
    Component commandMuteInfoDescription();

    @Message("command.nickname.argument.player")
    Component commandNicknameArgumentPlayer();

    @Message("command.nickname.argument.nickname")
    Component commandNicknameArgumentNickname();

    @Message("command.nickname.reset.description")
    Component commandNicknameResetDescription();

    @Message("command.nickname.set.description")
    Component commandNicknameSetDescription();

    @Message("command.nickname.description")
    Component commandNicknameDescription();

    @Message("command.nickname.others.reset.description")
    Component commandNicknameOthersResetDescription();

    @Message("command.nickname.others.set.description")
    Component commandNicknameOthersSetDescription();

    @Message("command.nickname.others.description")
    Component commandNicknameOthersDescription();

    @Message("command.reload.description")
    Component commandReloadDescription();

    @Message("command.reply.argument.message")
    Component commandReplyArgumentMessage();

    @Message("command.reply.description")
    Component commandReplyDescription();

    @Message("command.togglemsg.description")
    Component commandToggleMsgDescription();

    @Message("command.unignore.argument.player")
    Component commandUnignoreArgumentPlayer();

    @Message("command.unignore.argument.uuid")
    Component commandUnignoreArgumentUUID();

    @Message("command.unignore.description")
    Component commandUnignoreDescription();

    @Message("command.unmute.argument.player")
    Component commandUnmuteArgumentPlayer();

    @Message("command.unmute.argument.uuid")
    Component commandUnmuteArgumentUUID();

    @Message("command.unmute.description")
    Component commandUnmuteDescription();

    @Message("command.whisper.argument.player")
    Component commandWhisperArgumentPlayer();

    @Message("command.whisper.argument.message")
    Component commandWhisperArgumentMessage();

    @Message("command.whisper.description")
    Component commandWhisperDescription();

    @Message("command.updateusername.description")
    Component commandUpdateUsernameDescription();

    @Message("command.updateusername.argument.player")
    Component commandUpdateUsernameArgumentPlayer();

    @Message("command.updateusername.argument.uuid")
    Component commandUpdateUsernameArgumentUUID();

    @Message("command.updateusername.notupdated")
    void usernameNotUpdated(final Audience recipient);

    @Message("command.updateusername.fetching")
    void usernameFetching(final Audience audience);

    @Message("command.updateusername.updated")
    void usernameUpdated(final Audience audience, @Placeholder("newname") final String newName);

    @Message("command.party.pagination_header")
    Component commandPartyPaginationHeader(Component partyName);

    @Message("command.party.pagination_element")
    Component commandPartyPaginationElement(Component displayName, String username, Option online);

    @Message("command.party.created")
    void partyCreated(Audience audience, Component partyName);

    @Message("command.party.not_in_party")
    void notInParty(Audience audience);

    @Message("command.party.current_party")
    void currentParty(Audience audience, Component partyName);

    @Message("command.party.must_leave_current_first")
    void mustLeavePartyFirst(Audience audience);

    @Message("command.party.name_too_long")
    void partyNameTooLong(Audience audience);

    @Message("command.party.received_invite")
    void receivedPartyInvite(Audience audience, Component senderDisplayName, String senderUsername, Component partyName);

    @Message("command.party.sent_invite")
    void sentPartyInvite(Audience audience, Component recipientDisplayName, Component partyName);

    @Message("command.party.must_specify_invite")
    void mustSpecifyPartyInvite(Audience audience);

    @Message("command.party.no_pending_invites")
    void noPendingPartyInvites(Audience audience);

    @Message("command.party.no_invite_from")
    void noPartyInviteFrom(Audience audience, Component senderDisplayName);

    @Message("command.party.joined_party")
    void joinedParty(Audience audience, Component partyName);

    @Message("command.party.left_party")
    void leftParty(Audience audience, Component partyName);

    @Message("command.party.disbanded")
    void disbandedParty(Audience audience, Component partyName);

    @Message("command.party.cannot_disband_multiple_members")
    void cannotDisbandParty(Audience audience, Component partyName);

    @Message("command.party.must_be_in_party")
    void mustBeInParty(Audience audience);

    @Message("command.party.cannot_invite_self")
    void cannotInviteSelf(Audience audience);

    @Message("command.party.already_in_party")
    void alreadyInParty(Audience audience, Component displayName);

    @Message("command.party.description")
    Component partyDesc();

    @Message("command.party.create.description")
    Component partyCreateDesc();

    @Message("command.party.invite.description")
    Component partyInviteDesc();

    @Message("command.party.accept.description")
    Component partyAcceptDesc();

    @Message("command.party.leave.description")
    Component partyLeaveDesc();

    @Message("command.party.disband.description")
    Component partyDisbandDesc();

    @Message("party.player_joined")
    void playerJoinedParty(Audience audience, Component partyName, Component displayName);

    @Message("party.player_left")
    void playerLeftParty(Audience audience, Component partyName, Component displayName);

    @Message("party.cannot_use_channel")
    Component cannotUsePartyChannel(Audience audience);

    @Message("party.spy")
    void partySpy(Audience audience, Component message);

    @Message("deletemessage.prefix")
    Component deleteMessagePrefix();

    @Message("pagination.page_out_of_range")
    Component paginationOutOfRange(int page, int pages);

    @Message("pagination.click_for_next_page")
    Component paginationClickForNextPage();

    @Message("pagination.click_for_previous_page")
    Component paginationClickForPreviousPage();

    @Message("pagination.footer")
    Component paginationFooter(int page, int pages, Component buttons);

    /*
     * =============================================================
     * ======================= Integrations ========================
     * =============================================================
     */

    @Message("integrations.towny.cannot_use_alliance_channel")
    Component cannotUseAllianceChannel(Audience audience);

    @Message("integrations.towny.cannot_use_nation_channel")
    Component cannotUseNationChannel(Audience audience);

    @Message("integrations.towny.cannot_use_town_channel")
    Component cannotUseTownChannel(Audience audience);

    @Message("integrations.mcmmo.cannot_use_party_channel")
    Component cannotUseMcmmoPartyChannel(Audience audience);

    @Message("integrations.fuuid.cannot_use_faction_channel")
    Component cannotUseFactionChannel(Audience audience);

    @Message("integrations.fuuid.cannot_use_alliance_channel")
    Component cannotUseFactionAllianceChannel(Audience audience);

    @Message("integrations.fuuid.cannot_use_truce_channel")
    Component cannotUseTruceChannel(Audience audience);
}
