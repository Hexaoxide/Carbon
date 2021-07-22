package net.draycia.carbon.common.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonMessageSender implements IMessageSender<Audience, Component> {

    @Override
    public void send(final Audience receiver, final Component renderedMessage) {
        receiver.sendMessage(renderedMessage);
    }

}
