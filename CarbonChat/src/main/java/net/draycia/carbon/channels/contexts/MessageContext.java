package net.draycia.carbon.channels.contexts;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;

public interface MessageContext {
    ChatUser getSender();
    ChatUser getTarget();
    ChatChannel getChatChannel();
    Object getValue();
}
