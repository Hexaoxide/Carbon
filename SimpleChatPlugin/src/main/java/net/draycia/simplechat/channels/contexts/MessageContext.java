package net.draycia.simplechat.channels.contexts;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;

public interface MessageContext {
    ChatUser getSender();
    ChatUser getTarget();
    ChatChannel getChatChannel();
    Object getValue();
}
