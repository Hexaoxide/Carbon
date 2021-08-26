package net.draycia.carbon.common.channels.messages;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;

public interface ConfigChannelMessageService {

    // TODO: locale placeholders?
    @Message("channel.format")
    Component chatFormat(
        final Audience audience,
        @Placeholder UUID uuid,
        @Placeholder Component displayname,
        @Placeholder String username,
        @Placeholder Component message
    );

}
