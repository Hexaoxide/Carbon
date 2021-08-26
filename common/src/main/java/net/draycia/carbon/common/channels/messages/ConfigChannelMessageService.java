package net.draycia.carbon.common.channels.messages;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;

public interface ConfigChannelMessageService {

    // TODO: locale placeholders?
    @Message("")
    Component chatFormat(
        final Audience audience,
        UUID uuid,
        Component displayname,
        String username,
        Component message
    );

}
