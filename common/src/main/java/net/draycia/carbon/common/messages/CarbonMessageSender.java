package net.draycia.carbon.common.messages;

import com.proximyst.moonshine.message.IMessageSender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public class CarbonMessageSender implements IMessageSender<Audience, Component> {

    @Override
    public void sendMessage(final Audience receiver, final Component message) {
        receiver.sendMessage(message);
    }

}
