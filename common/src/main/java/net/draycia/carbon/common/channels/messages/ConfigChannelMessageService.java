package net.draycia.carbon.common.channels.messages;

import java.util.UUID;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.util.ChatType;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.annotation.Message;
import net.kyori.moonshine.annotation.Placeholder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface ConfigChannelMessageService {

    // TODO: locale placeholders?
    @Message("channel.format")
    @ChatType(MessageType.CHAT)
    Component chatFormat(
        final SourcedAudience audience,
        @Placeholder UUID uuid,
        @Placeholder Key channel,
        @Placeholder("display_name") Component displayName,
        @Placeholder String username,
        @Placeholder Component message
    );

}
