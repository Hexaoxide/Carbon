package net.draycia.carbon.common.messages;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class SourcedMessageSender implements IMessageSender<SourcedAudience, RenderedMessage> {

    @Override
    public void send(final SourcedAudience receiver, final RenderedMessage renderedMessage) {
        if (receiver.sender() instanceof CarbonPlayer sender) {
            receiver.recipient().sendMessage(Identity.identity(sender.uuid()), renderedMessage.component(), renderedMessage.messageType());
        } else {
            receiver.recipient().sendMessage(Identity.nil(), renderedMessage.component(), renderedMessage.messageType());
        }
    }

}
