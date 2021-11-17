package net.draycia.carbon.common.messages;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonMessageSender implements IMessageSender<Audience, RenderedMessage> {

    @Override
    public void send(final Audience receiver, final RenderedMessage renderedMessage) {
        if (receiver instanceof SourcedAudience sourcedAudience) {
            if (sourcedAudience.sender() instanceof CarbonPlayer sender && !sender.hasPermission("carbon.hideidentity")) {
                sourcedAudience.recipient().sendMessage(Identity.identity(sender.uuid()), renderedMessage.component(), renderedMessage.messageType());
            } else {
                sourcedAudience.recipient().sendMessage(Identity.nil(), renderedMessage.component(), renderedMessage.messageType());
            }
        } else {
            receiver.sendMessage(Identity.nil(), renderedMessage.component(), renderedMessage.messageType());
        }
    }

}
