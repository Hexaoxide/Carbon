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
package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessages;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.messages.SourcedMessageSender;
import net.draycia.carbon.common.messages.SourcedReceiverResolver;
import net.draycia.carbon.common.messages.placeholders.BooleanPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.IntPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.UUIDPlaceholderResolver;
import net.draycia.carbon.common.util.Exceptions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.Moonshine;
import net.kyori.moonshine.exception.scan.UnscannableMethodException;
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy;
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class ConfigChatChannel implements ChatChannel {

    protected transient @MonotonicNonNull @Inject CarbonServer server;
    private transient @MonotonicNonNull @Inject CarbonMessageRenderer renderer;

    @Comment("""
        The channel's key, used to track the channel.
        You only need to change the second part of the key. "global" by default.
        The value is what's used in commands, this is probably what you want to change.
        """)
    protected @Nullable Key key = Key.key("carbon", "global");

    @Comment("""
        The permission required to use the /channel <channelname> and /<channelname> commands.
        
        Assuming permission = "carbon.channel.global"
        To read messages you must have the permission carbon.channel.global.see
        To send messages you must have the permission carbon.channel.global.speak
        """)
    private @Nullable String permission = null;

    @Setting("format")
    @Comment("The chat formats for this channel.")
    protected @Nullable ConfigChannelMessageSource messageSource = new ConfigChannelMessageSource();

    @Comment("Messages will be sent in this channel if they start with this prefix. (Leave empty/blank to disable quick prefix for this channel)")
    private @Nullable String quickPrefix = "";

    private @Nullable Boolean shouldRegisterCommands = true;

    private @Nullable String commandName = null;

    protected @Nullable List<String> commandAliases = Collections.emptyList();

    private transient @Nullable ConfigChannelMessages carbonMessages = null;

    @Comment("""
        The distance players must be within to see each other's messages.
        A value of '0' requires that both players are in the same world.
        On velocity, '0' requires that both players are in the same server.
        """)
    private int radius = -1;

    @Comment("""
        If true, players will be able to see if they're not sending messages to anyone
        because they're out of range from the radius.
        """)
    private boolean emptyRadiusRecipientsMessage = true;

    @Override
    public @Nullable String quickPrefix() {
        if (this.quickPrefix == null || this.quickPrefix.isBlank()) {
            return null;
        }

        return this.quickPrefix;
    }

    @Override
    public boolean shouldRegisterCommands() {
        return Objects.requireNonNullElse(this.shouldRegisterCommands, true);
    }

    @Override
    public String commandName() {
        return Objects.requireNonNullElse(this.commandName, this.key.value());
    }

    @Override
    public List<String> commandAliases() {
        return Objects.requireNonNullElse(this.commandAliases, Collections.emptyList());
    }

    @Override
    public @NotNull Component render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        final Component originalMessage
    ) {
        return this.carbonMessages().chatFormat(
            SourcedAudience.of(sender, recipient),
            sender.uuid(),
            this.key(),
            sender.displayName(),
            sender.username(),
            message,
            Component.text("null")
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer carbonPlayer) {
        return ChannelPermissionResult.allowedIf(text("Insufficient permissions!"), () ->
            carbonPlayer.hasPermission(this.permission() + ".speak")); // carbon.channels.local.speak
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return ChannelPermissionResult.allowedIf(empty(), () -> player.hasPermission(this.permission() + ".see") && !player.leftChannels().contains(this.key));
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final List<Audience> recipients = new ArrayList<>();

        for (final CarbonPlayer player : this.server.players()) {
            if (this.hearingPermitted(player).permitted()) {
                recipients.add(player);
            }
        }

        // console too!
        recipients.add(this.server.console());

        return recipients;
    }

    @Override
    public @NonNull Key key() {
        return Objects.requireNonNull(this.key);
    }

    public String messageFormat(final CarbonPlayer sender) {
        return this.messageSource.messageOf(SourcedAudience.of(sender, sender), "");
    }

    private ConfigChannelMessages loadMessages() {
        final SourcedReceiverResolver serverReceiverResolver = new SourcedReceiverResolver();
        final ComponentPlaceholderResolver<SourcedAudience> componentPlaceholderResolver = new ComponentPlaceholderResolver<>();
        final UUIDPlaceholderResolver<SourcedAudience> uuidPlaceholderResolver = new UUIDPlaceholderResolver<>();
        final StringPlaceholderResolver<SourcedAudience> stringPlaceholderResolver = new StringPlaceholderResolver<>();
        final KeyPlaceholderResolver<SourcedAudience> keyPlaceholderResolver = new KeyPlaceholderResolver<>();
        final BooleanPlaceholderResolver<SourcedAudience> booleanPlaceholderResolver = new BooleanPlaceholderResolver<>();
        final SourcedMessageSender carbonMessageSender = new SourcedMessageSender();

        try {
            return Moonshine.<ConfigChannelMessages, SourcedAudience>builder(new TypeToken<ConfigChannelMessages>() {})
                .receiverLocatorResolver(serverReceiverResolver, 0)
                .sourced(this.messageSource)
                .rendered(this.renderer.asSourced())
                .sent(carbonMessageSender)
                .resolvingWithStrategy(new StandardPlaceholderResolverStrategy<>(new StandardSupertypeThenInterfaceSupertypeStrategy(false)))
                .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
                .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
                .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
                .weightedPlaceholderResolver(Integer.class, new IntPlaceholderResolver<>(), 0)
                .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
                .weightedPlaceholderResolver(Boolean.class, booleanPlaceholderResolver, 0)
                .create(this.getClass().getClassLoader());
        } catch (final UnscannableMethodException e) {
            throw Exceptions.rethrow(e);
        }
    }

    protected ConfigChannelMessages carbonMessages() {
        if (this.carbonMessages == null) {
            this.carbonMessages = this.loadMessages();
        }

        return requireNonNull(this.carbonMessages, "Channel message service must not be null!");
    }

    @Override
    public String permission() {
        if (this.permission == null) {
            return "carbon.channel." + this.key().value();
        }

        return this.permission;
    }

    @Override
    public double radius() {
        return this.radius;
    }

    @Override
    public boolean emptyRadiusRecipientsMessage() {
        return this.emptyRadiusRecipientsMessage;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConfigChatChannel otherChannel)) {
            return false;
        }
        return otherChannel.key().equals(this.key());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key());
    }

}
