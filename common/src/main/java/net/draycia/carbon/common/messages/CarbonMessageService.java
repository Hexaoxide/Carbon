package net.draycia.carbon.common.messages;

import com.proximyst.moonshine.annotation.Message;
import com.proximyst.moonshine.annotation.Placeholder;
import com.proximyst.moonshine.annotation.Receiver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public interface CarbonMessageService {

    @Message("example.command.hello")
    void exampleCommandFeedback(
        @Receiver final Audience audience,
        @Placeholder final Component plugin
    );

    @Message("unsupported.legacy")
    void unsupportedLegacyChar(
        @Receiver final Audience audience,
        @Placeholder final String message
    );

}
