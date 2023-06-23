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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessages;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.SourcedMessageSender;
import net.draycia.carbon.common.messages.SourcedReceiverResolver;
import net.draycia.carbon.common.messages.placeholders.BooleanPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.UUIDPlaceholderResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
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
public final class ConfigChatChannel implements ChatChannel {

    private transient @MonotonicNonNull @Inject CarbonChat carbonChat;

    @Comment("""
        The channel's key, used to track the channel.
        You only need to change the second part of the key. "global" by default.
        The value is what's used in commands, this is probably what you want to change.
        """)
    private @Nullable Key key = Key.key("carbon", "global");

    @Comment("""
        The permission required to use the /channel <channelname> and /<channelname> commands.
        
        Assuming permission = "carbon.channel.global"
        To read messages you must have the permission carbon.channel.global.see
        To send messages you must have the permission carbon.channel.global.speak
        """)
    private @Nullable String permission = null;

    @Setting("format")
    @Comment("The chat formats for this channel.")
    private @Nullable ConfigChannelMessageSource messageSource = new ConfigChannelMessageSource();

    @Comment("Messages will be sent in this channel if they start with this prefix.")
    private @Nullable String quickPrefix = null;

    private @Nullable Boolean shouldRegisterCommands = true;

    private @Nullable String commandName = null;

    private @Nullable List<String> commandAliases = Collections.emptyList();

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
            new SourcedAudience(sender, recipient),
            sender.uuid(),
            this.key(),
            Objects.requireNonNull(CarbonPlayer.renderName(sender)),
            sender.username(),
            message
        );
    }

    public static final Map<String, TagResolver> DEFAULT_TAGS = Map.ofEntries(
        Map.entry("hover", StandardTags.hoverEvent()),
        Map.entry("click", StandardTags.clickEvent()),
        Map.entry("color", StandardTags.color()),
        Map.entry("keybind", StandardTags.keybind()),
        Map.entry("translatable", StandardTags.translatable()),
        Map.entry("insertion", StandardTags.insertion()),
        Map.entry("font", StandardTags.font()),
        // Decoration tags are handled separately
        //Map.entry("decoration", StandardTags.decoration()),
        Map.entry("gradient", StandardTags.gradient()),
        Map.entry("rainbow", StandardTags.rainbow()),
        Map.entry("reset", StandardTags.reset()),
        Map.entry("newline", StandardTags.newline())
    );

    public static Component parseMessageTags(final Commander sender, final String message) {
        return parseMessageTags(message, sender::hasPermission);
    }

    public static Component parseMessageTags(final CarbonPlayer sender, final String message) {
        return parseMessageTags(message, sender::hasPermission);
    }

    public static Component parseMessageTags(final String message, final Predicate<String> permission) {
        if (!permission.test("carbon.messagetags")) {
            return Component.text(message);
        }

        final TagResolver.Builder resolver = TagResolver.builder();

        for (final Map.Entry<String, TagResolver> entry : DEFAULT_TAGS.entrySet()) {
            if (permission.test("carbon.messagetags." + entry.getKey())) {
                resolver.resolver(entry.getValue());
            }
        }

        for (final TextDecoration decoration : TextDecoration.values()) {
            if (!permission.test("carbon.messagetags." + decoration.name())) {
                continue;
            }

            resolver.resolver(StandardTags.decorations(decoration));
        }

        final MiniMessage miniMessage = MiniMessage.builder().tags(resolver.build()).build();

        return miniMessage.deserialize(message);
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

        for (final CarbonPlayer player : this.carbonChat.server().players()) {
            if (this.hearingPermitted(player).permitted()) {
                recipients.add(player);
            }
        }

        // console too!
        recipients.add(this.carbonChat.server().console());

        return recipients;
    }

    @Override
    public Set<CarbonPlayer> filterRecipients(final CarbonPlayer sender, final Set<CarbonPlayer> recipients) {
        try {
            recipients.removeIf(it -> !this.hearingPermitted(it).permitted());
        } catch (final UnsupportedOperationException ignored) {

        }

        return recipients;
    }

    @Override
    public @NonNull Key key() {
        return Objects.requireNonNull(this.key);
    }

    public String messageFormat(final CarbonPlayer sender) {
        return this.messageSource.messageOf(new SourcedAudience(sender, sender), "");
    }

    private @Nullable ConfigChannelMessages loadMessages() {
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
                .rendered(this.carbonChat.messageRenderer())
                .sent(carbonMessageSender)
                .resolvingWithStrategy(new StandardPlaceholderResolverStrategy<>(new StandardSupertypeThenInterfaceSupertypeStrategy(false)))
                .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
                .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
                .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
                .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
                .weightedPlaceholderResolver(Boolean.class, booleanPlaceholderResolver, 0)
                .create(this.getClass().getClassLoader());
        } catch (final UnscannableMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ConfigChannelMessages carbonMessages() {
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
        if (!(other instanceof BasicChatChannel otherChannel)) {
            return false;
        }

        if (!(otherChannel.commandName().equals(this.commandName()))) {
            return false;
        }

        if (!(Objects.equals(otherChannel.quickPrefix(), this.quickPrefix()))) {
            return false;
        }

        if (!(Objects.equals(otherChannel.permission(), this.permission()))) {
            return false;
        }

        if (otherChannel.radius() != this.radius()) {
            return false;
        }

        return otherChannel.key().equals(this.key());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.commandName(), this.quickPrefix(), this.permission(), this.radius(), this.key());
    }

}
