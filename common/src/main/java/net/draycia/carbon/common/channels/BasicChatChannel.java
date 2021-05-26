package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class BasicChatChannel implements ChatChannel {

    private final Key key = Key.key("carbon", "vanilla");

    private final CarbonMessageService service;

    public BasicChatChannel(final CarbonMessageService service) {
        this.service = service;
    }

    @Override
    public @Nullable Component render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        Component originalMessage
    ) {
        // TODO: Once per-user is setup, use the service for the recipient?
        // TODO: Or, have the MessageSource do the per-use for us.
        return service.basicChatFormat(
            recipient,
            sender.uuid(),
            sender.displayName(),
            sender.username()
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer carbonPlayer) {
        return ChannelPermissionResult.allowed();
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer carbonPlayer) {
        return ChannelPermissionResult.allowed();
    }

    @Override
    public @NonNull Key key() {
        return this.key;
    }
}
