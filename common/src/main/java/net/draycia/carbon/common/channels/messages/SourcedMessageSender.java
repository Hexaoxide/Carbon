package net.draycia.carbon.common.channels.messages;

import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class SourcedMessageSender implements IMessageSender<SourcedAudience, Component> {

    @Override
    public void send(final SourcedAudience receiver, final Component renderedMessage) {
        receiver.recipient().sendMessage(renderedMessage);
    }

}
