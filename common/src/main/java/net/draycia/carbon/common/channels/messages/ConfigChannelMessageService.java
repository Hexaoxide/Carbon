package net.draycia.carbon.common.channels.messages;

import com.proximyst.moonshine.annotation.Message;
import com.proximyst.moonshine.annotation.Placeholder;
import com.proximyst.moonshine.annotation.Receiver;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public interface ConfigChannelMessageService {

    // TODO: locale placeholders?
    @Message("channel.format")
    Component chatFormat(
        @Receiver final Audience audience,
        @Placeholder UUID uuid,
        @Placeholder Component displayname,
        @Placeholder String username,
        @Placeholder Component message
    );

}
