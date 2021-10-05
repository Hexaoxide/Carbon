package net.draycia.carbon.common.channels.messages;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class SourcedMessageSender implements IMessageSender<SourcedAudience, Component> {

    @Override
    public void send(final SourcedAudience receiver, final Component renderedMessage) {
        if (receiver.sender() instanceof CarbonPlayer player && !player.hasPermission("carbon.noidentity")) {
            receiver.recipient().sendMessage(Identity.identity(player.uuid()), renderedMessage);
        } else {
            receiver.recipient().sendMessage(Identity.nil(), renderedMessage);
        }
    }

}
