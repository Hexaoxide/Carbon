package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.proximyst.moonshine.Moonshine;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageService;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.messages.CarbonMessageParser;
import net.draycia.carbon.common.messages.CarbonMessageSender;
import net.draycia.carbon.common.messages.ComponentPlaceholderResolver;
import net.draycia.carbon.common.messages.ServerReceiverResolver;
import net.draycia.carbon.common.messages.UUIDPlaceholderResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import static net.kyori.adventure.text.Component.text;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public final class ConfigChatChannel implements ChatChannel {

    @Comment("""
        The channel's key, used to track the channel.
        The key can stay "carbon".
        The value is what's used in commands, this is probably what you want to change.
        """)
    private Key key = Key.key("carbon", "basic");

    @Comment("""
        The permission required to use the channel.
        To read messages you must have the permission carbon.channel.basic.see
        To send messages you must have the permission carbon.channel.basic.speak
        If you want to give both, grant carbon.channel.basic or carbon.channel.basic.*
        """)
    private String permission = "carbon.channel.basic";

    @Setting("format")
    @Comment("stuff here lol")
    private ConfigChannelMessageSource messageSource = new ConfigChannelMessageSource();

    @Inject
    private transient Injector injector;

    @Inject
    private transient CarbonServer carbonServer;

    private transient @Nullable ConfigChannelMessageService messageService = null;

    @Override
    public @NotNull Component render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        final Component originalMessage
    ) {
        return this.messageService().chatFormat(
            recipient,
            sender.uuid(),
            sender.displayName(),
            sender.username(),
            message
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer carbonPlayer) {
        return ChannelPermissionResult.allowedIf(text("Insufficient permissions!"), () ->
            carbonPlayer.hasPermission(this.permission + ".speak")); // carbon.channels.local.speak
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final Audience audience) {
        if (audience instanceof CarbonPlayer carbonPlayer) {
            return ChannelPermissionResult.allowedIf(text("Insufficient permissions!"), () ->
                carbonPlayer.hasPermission(this.permission + ".see")); // carbon.channels.local.see
        } else {
            return ChannelPermissionResult.allowed();
        }
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final List<Audience> recipients = new ArrayList<>();

        for (final CarbonPlayer player : this.carbonServer.players()) {
            if (this.hearingPermitted(player).permitted()) {
                recipients.add(player);
            }
        }

        // console too!
        recipients.add(this.carbonServer.console());

        return recipients;
    }

    @Override
    public Set<Audience> filterRecipients(final CarbonPlayer sender, final Set<Audience> recipients) {
        recipients.removeIf(it -> !this.hearingPermitted(it).permitted());

        return recipients;
    }

    @Override
    public @NonNull Key key() {
        return this.key;
    }

    private ConfigChannelMessageService createMessageService() {
        final ServerReceiverResolver serverReceiverResolver = this.injector.getInstance(ServerReceiverResolver.class);
        final ComponentPlaceholderResolver<Audience> componentPlaceholderResolver = this.injector.getInstance(ComponentPlaceholderResolver.class);
        final UUIDPlaceholderResolver<Audience> uuidPlaceholderResolver = this.injector.getInstance(UUIDPlaceholderResolver.class);
        final CarbonMessageParser carbonMessageParser = this.injector.getInstance(CarbonMessageParser.class);
        final CarbonMessageSender carbonMessageSender = this.injector.getInstance(CarbonMessageSender.class);

        return Moonshine.<Audience>builder()
            .receiver(serverReceiverResolver)
            .placeholder(Component.class, componentPlaceholderResolver)
            .placeholder(UUID.class, uuidPlaceholderResolver)
            .source(this.messageSource)
            .parser(carbonMessageParser)
            .sender(carbonMessageSender)
            .create(ConfigChannelMessageService.class, this.getClass().getClassLoader());
    }

    private ConfigChannelMessageService messageService() {
        if (this.messageService == null) {
            this.messageService = this.createMessageService();
        }

        return this.messageService;
    }

}
