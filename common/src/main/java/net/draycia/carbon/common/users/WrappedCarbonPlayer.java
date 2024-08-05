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
package net.draycia.carbon.common.users;

import io.github.miniplaceholders.api.MiniPlaceholders;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.api.util.InventorySlot;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.integration.miniplaceholders.MiniPlaceholdersExpansion;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.messages.TagPermissions;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNullElse;

@DefaultQualifier(NonNull.class)
public abstract class WrappedCarbonPlayer implements CarbonPlayer {

    protected final CarbonPlayerCommon carbonPlayerCommon;

    protected WrappedCarbonPlayer(final CarbonPlayerCommon carbonPlayerCommon) {
        this.carbonPlayerCommon = carbonPlayerCommon;
    }

    public CarbonPlayerCommon carbonPlayerCommon() {
        return this.carbonPlayerCommon;
    }

    public @Nullable User user() {
        return LuckPermsProvider.get().getUserManager().getUser(this.uuid());
    }

    public Component parseMessageTags(final String message) {
        final TagResolver.Builder resolver = TagResolver.builder();

        if (MiniPlaceholdersExpansion.miniPlaceholdersLoaded() && this.hasPermission("carbon.chatplaceholders")) {
            resolver.resolver(MiniPlaceholders.getGlobalPlaceholders());
            resolver.resolver(MiniPlaceholders.getAudiencePlaceholders(this));
        }

        return TagPermissions.parseTags(TagPermissions.MESSAGE, message, this::hasPermission, resolver);
    }

    @Override
    public boolean awareOf(final CarbonPlayer other) {
        if (other.vanished()) {
            return this.hasPermission("carbon.whisper.vanished");
        }

        return true;
    }

    @Override
    public Set<UUID> ignoring() {
        return this.carbonPlayerCommon.ignoring();
    }

    @Override
    public boolean ignoring(final UUID player) {
        return this.carbonPlayerCommon.ignoring(player);
    }

    @Override
    public boolean ignoring(final CarbonPlayer player) {
        return this.carbonPlayerCommon.ignoring(player);
    }

    @Override
    public void ignoring(final UUID player, final boolean nowIgnoring) {
        this.carbonPlayerCommon.ignoring(player, nowIgnoring);
    }

    @Override
    public void ignoring(final CarbonPlayer player, final boolean nowIgnoring) {
        this.carbonPlayerCommon.ignoring(player, nowIgnoring);
    }

    @Override
    public boolean ignoringDirectMessages() {
        return this.carbonPlayerCommon.ignoringDirectMessages();
    }

    @Override
    public void ignoringDirectMessages(final boolean ignoring) {
        this.carbonPlayerCommon.ignoringDirectMessages(ignoring);
    }

    @Override
    public boolean hasPermission(final String permission) {
        final @Nullable User user = this.user();

        if (user == null) {
            return false;
        }

        final var data = user.getCachedData().getPermissionData(user.getQueryOptions());
        return data.checkPermission(permission) == Tristate.TRUE;
    }

    @Override
    public String primaryGroup() {
        final @Nullable User user = this.user();

        if (user == null) {
            return "default";
        }

        return user.getPrimaryGroup();
    }

    @Override
    public List<String> groups() {
        final @Nullable User user = this.user();

        if (user == null) {
            return List.of("default");
        }

        final var groups = new ArrayList<String>();

        for (final var group : user.getInheritedGroups(user.getQueryOptions())) {
            groups.add(group.getName());
        }

        return groups;
    }

    @Override
    public String username() {
        return this.carbonPlayerCommon.username();
    }

    // take care not to call get(Identity.DISPLAY_NAME) on a CarbonPlayer
    // from this method - it would result in a stack overflow when pointers
    // are retrieved from EmptyAudienceWithPointers
    @Override
    public Component displayName() {
        final @Nullable Component nick = this.nickname();
        if (nick != null) {
            final PrimaryConfig.NicknameSettings nicknames = this.carbonPlayerCommon.configManager().primaryConfig().nickname();

            if (nicknames.skipFormatWhenNameMatches) {
                final String plainNick = PlainTextComponentSerializer.plainText().serialize(nick);
                if (plainNick.equals(this.username())) {
                    return nick;
                }
            }

            try {
                return this.carbonPlayerCommon.messageRenderer().render(
                    SourcedAudience.of(this, this),
                    nicknames.format,
                    Map.of("username", Tag.preProcessParsed(this.username()), "nickname", Tag.selfClosingInserting(nick)),
                    null,
                    null
                );
            } catch (final StackOverflowError overflow) {
                throw new RuntimeException("Invalid nickname format '%s'. Makes circular reference to CarbonPlayer#displayName().".formatted(nicknames.format), overflow);
            }
        }
        return this.platformDisplayName().orElseGet(() -> Component.text(this.username()));
    }

    protected abstract Optional<Component> platformDisplayName();

    @Override
    public boolean hasNickname() {
        return this.carbonPlayerCommon.hasNickname();
    }

    @Override
    public @Nullable Component nickname() {
        return this.carbonPlayerCommon.nickname();
    }

    @Override
    public void nickname(final @Nullable Component nickname) {
        this.carbonPlayerCommon.nickname(nickname);
    }

    @Override
    public UUID uuid() {
        return this.carbonPlayerCommon.uuid();
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        return this.carbonPlayerCommon.createItemHoverComponent(slot);
    }

    @Override
    public @Nullable Locale locale() {
        return this.carbonPlayerCommon.locale();
    }

    @Override
    public ChannelMessage channelForMessage(final Component message) {
        final String text = PlainTextComponentSerializer.plainText().serialize(message);
        Component formattedMessage = message;

        ChatChannel channel = requireNonNullElse(this.selectedChannel(), this.carbonPlayerCommon.channelRegistry().defaultChannel());

        for (final Key channelKey : this.carbonPlayerCommon.channelRegistry().keys()) {
            final ChatChannel chatChannel = this.carbonPlayerCommon.channelRegistry().channelOrThrow(channelKey);
            final @Nullable String prefix = chatChannel.quickPrefix();

            if (prefix == null) {
                continue;
            }

            if (text.startsWith(prefix) && chatChannel.permissions().speechPermitted(this).permitted()) {
                channel = chatChannel;
                formattedMessage = formattedMessage.replaceText(TextReplacementConfig.builder()
                    .once()
                    .matchLiteral(channel.quickPrefix())
                    .replacement(Component.empty())
                    .build());
                break;
            }
        }

        return new ChannelMessage(formattedMessage, channel);
    }

    @Override
    public @Nullable ChatChannel selectedChannel() {
        return this.carbonPlayerCommon.selectedChannel();
    }

    @Override
    public void selectedChannel(final @Nullable ChatChannel chatChannel) {
        this.carbonPlayerCommon.selectedChannel(chatChannel);
    }

    @Override
    public boolean muted() {
        return this.carbonPlayerCommon.muted();
    }

    @Override
    public void muted(final boolean muted) {
        this.carbonPlayerCommon.muted(muted);
    }

    @Override
    public boolean deafened() {
        return this.carbonPlayerCommon.deafened();
    }

    @Override
    public void deafened(final boolean deafened) {
        this.carbonPlayerCommon.deafened(deafened);
    }

    @Override
    public boolean spying() {
        return this.carbonPlayerCommon.spying();
    }

    @Override
    public void spying(final boolean spying) {
        this.carbonPlayerCommon.spying(spying);
    }

    @Override
    public void sendMessageAsPlayer(final String message) {
        this.carbonPlayerCommon.sendMessageAsPlayer(message);
    }

    @Override
    public boolean online() {
        return this.carbonPlayerCommon.online();
    }

    @Override
    public @Nullable UUID whisperReplyTarget() {
        return this.carbonPlayerCommon.whisperReplyTarget();
    }

    @Override
    public void whisperReplyTarget(final @Nullable UUID uuid) {
        this.carbonPlayerCommon.whisperReplyTarget(uuid);
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.carbonPlayerCommon.lastWhisperTarget();
    }

    @Override
    public void lastWhisperTarget(final @Nullable UUID uuid) {
        this.carbonPlayerCommon.lastWhisperTarget(uuid);
    }

    @Override
    public @NotNull Identity identity() {
        return this.carbonPlayerCommon.identity();
    }

    @Override
    public boolean vanished() {
        return this.carbonPlayerCommon.vanished();
    }

    @Override
    public List<Key> leftChannels() {
        return this.carbonPlayerCommon.leftChannels();
    }

    @Override
    public void joinChannel(final ChatChannel channel) {
        this.carbonPlayerCommon.joinChannel(channel);
    }

    @Override
    public void leaveChannel(final ChatChannel channel) {
        this.carbonPlayerCommon.leaveChannel(channel);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final WrappedCarbonPlayer that = (WrappedCarbonPlayer) other;

        return this.carbonPlayerCommon.equals(that.carbonPlayerCommon);
    }

    @Override
    public int hashCode() {
        return this.carbonPlayerCommon.hashCode();
    }

    public @Nullable UUID partyId() {
        return this.carbonPlayerCommon.partyId();
    }

    @Override
    public CompletableFuture<@Nullable Party> party() {
        return this.carbonPlayerCommon.party();
    }

    public void party(final @Nullable Party party) {
        this.carbonPlayerCommon.party(party);
    }

}
