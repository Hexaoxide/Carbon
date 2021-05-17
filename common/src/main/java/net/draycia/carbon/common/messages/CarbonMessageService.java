package net.draycia.carbon.common.messages;

import com.proximyst.moonshine.annotation.Message;
import com.proximyst.moonshine.annotation.Placeholder;
import com.proximyst.moonshine.annotation.Receiver;
import net.kyori.adventure.audience.Audience;
<<<<<<< HEAD
import net.kyori.adventure.text.Component;
=======

import java.awt.*;
>>>>>>> Initial pass - 1/?

public interface CarbonMessageService {

    @Message("example.command.hello")
<<<<<<< HEAD
    void exampleCommandFeedback(
        @Receiver final Audience audience,
        @Placeholder final Component plugin
    );

    @Message("unsupported.legacy")
    void unsupportedLegacyChar(
        @Receiver final Audience audience,
        @Placeholder final String message
    );
=======
    void exampleCommandFeedback(@Receiver final Audience audience,
                                @Placeholder final Component plugin);
>>>>>>> Initial pass - 1/?

}
