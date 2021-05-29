package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@Singleton
@DefaultQualifier(NonNull.class)
public class BasicChatChannel implements ChatChannel {

    private final Key key = Key.key("carbon", "vanilla");

    private final CarbonMessageService service;
    private final CarbonChat carbonChat;

    @Inject
    private BasicChatChannel(
        final CarbonMessageService service,
        final CarbonChat carbonChat
        ) {
        this.service = service;
        this.carbonChat = carbonChat;
    }

    @Override
    public @Nullable @NotNull Component render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        final Component originalMessage
    ) {
        // TODO: Once per-user is setup, use the service for the recipient?
        // TODO: Or, have the MessageSource do the per-use for us.
        return this.service.basicChatFormat(
            recipient,
            sender.uuid(),
            sender.displayName(),
            sender.username(),
            message
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
    public @NonNull Key key() {
        return this.key;
    }

}
