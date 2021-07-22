package net.draycia.carbon.common.messages;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;

public interface CarbonMessageService {

    @Message("example.command.hello")
    void exampleCommandFeedback(
        final Audience audience,
        final Component plugin
    );

    @Message("unsupported.legacy")
    void unsupportedLegacyChar(
        final Audience audience,
        final String message
    );

    @Message("test.phrase")
    void localeTestMessage(final Audience audience);

    @Message("channel.format.basic")
    Component basicChatFormat(
        final Audience audience,
        UUID uuid,
        Component displayname,
        String username,
        Component message
    );

    @Message("mute.spy.prefix")
    Component muteSpyPrefix(final Audience audience);

}
