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
package net.draycia.carbon.common.channels;

import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageService;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.messages.SourcedMessageSender;
import net.draycia.carbon.common.messages.SourcedReceiverResolver;
import net.draycia.carbon.common.messages.placeholders.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.KeyPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.StringPlaceholderResolver;
import net.draycia.carbon.common.messages.placeholders.UUIDPlaceholderResolver;
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
public final class ConfigChatChannel implements ChatChannel {

    @Comment("""
        The channel's key, used to track the channel.
        You only need to change the second part of the key. "global" by default.
        The value is what's used in commands, this is probably what you want to change.
        """)
    private @Nullable Key key = Key.key("carbon", "global");

    @Comment("""
        The permission required to use the channel.
        To read messages you must have the permission carbon.channel.global.see
        To send messages you must have the permission carbon.channel.global
        """)
    private @Nullable String permission = "carbon.channel.global";

    @Setting("format")
    @Comment("The chat formats for this channel.")
    private @Nullable ConfigChannelMessageSource messageSource = new ConfigChannelMessageSource();

    @Comment("Messages will be sent in this channel if they start with this prefix.")
    private @Nullable String quickPrefix = "";

    private @Nullable Boolean shouldRegisterCommands = true;

    private @Nullable String commandName = null;

    private @Nullable List<String> commandAliases = Collections.emptyList();

    private int radius = -1;

    private transient @Nullable ConfigChannelMessageService messageService = null;

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
    public @NotNull RenderedMessage render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        final Component originalMessage
    ) {
        return this.messageService().chatFormat(
            new SourcedAudience(sender, recipient),
            sender.uuid(),
            this.key(),
            Objects.requireNonNull(CarbonPlayer.renderName(sender)),
            sender.username(),
            message
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer carbonPlayer) {
        return ChannelPermissionResult.allowedIf(text("Insufficient permissions!"), () ->
            carbonPlayer.hasPermission(this.permission() + ".speak")); // carbon.channels.local.speak
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return ChannelPermissionResult.allowedIf(empty(), () -> player.hasPermission(this.permission() + ".see"));
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final List<Audience> recipients = new ArrayList<>();

        for (final CarbonPlayer player : CarbonChatProvider.carbonChat().server().players()) {
            if (this.hearingPermitted(player).permitted()) {
                recipients.add(player);
            }
        }

        // console too!
        recipients.add(CarbonChatProvider.carbonChat().server().console());

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
        return this.key;
    }

    private @Nullable ConfigChannelMessageService createMessageService() {
        final SourcedReceiverResolver serverReceiverResolver = new SourcedReceiverResolver();
        final ComponentPlaceholderResolver<SourcedAudience> componentPlaceholderResolver = new ComponentPlaceholderResolver<>();
        final UUIDPlaceholderResolver<SourcedAudience> uuidPlaceholderResolver = new UUIDPlaceholderResolver<>();
        final StringPlaceholderResolver<SourcedAudience> stringPlaceholderResolver = new StringPlaceholderResolver<>();
        final KeyPlaceholderResolver<SourcedAudience> keyPlaceholderResolver = new KeyPlaceholderResolver<>();
        final SourcedMessageSender carbonMessageSender = new SourcedMessageSender();

        try {
            return Moonshine.<ConfigChannelMessageService, SourcedAudience>builder(new TypeToken<ConfigChannelMessageService>() {})
                .receiverLocatorResolver(serverReceiverResolver, 0)
                .sourced(this.messageSource)
                .rendered(CarbonChatProvider.carbonChat().messageRenderer())
                .sent(carbonMessageSender)
                .resolvingWithStrategy(new StandardPlaceholderResolverStrategy<>(new StandardSupertypeThenInterfaceSupertypeStrategy(false)))
                .weightedPlaceholderResolver(Component.class, componentPlaceholderResolver, 0)
                .weightedPlaceholderResolver(UUID.class, uuidPlaceholderResolver, 0)
                .weightedPlaceholderResolver(String.class, stringPlaceholderResolver, 0)
                .weightedPlaceholderResolver(Key.class, keyPlaceholderResolver, 0)
                .create(this.getClass().getClassLoader());
        } catch (final UnscannableMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ConfigChannelMessageService messageService() {
        if (this.messageService == null) {
            this.messageService = this.createMessageService();
        }

        return requireNonNull(this.messageService, "Channel message service must not be null!");
    }

    @Override
    public @MonotonicNonNull String permission() {
        return this.permission;
    }

}
