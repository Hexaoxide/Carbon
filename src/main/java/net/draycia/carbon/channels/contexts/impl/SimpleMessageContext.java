package net.draycia.carbon.channels.contexts.impl;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.channels.contexts.MessageContext;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimpleMessageContext implements MessageContext {
  @NonNull
  private final ChatUser sender;

  @NonNull
  private final ChatUser target;

  private final ChatChannel chatChannel;
  private final Object value;

  public SimpleMessageContext(@NonNull ChatUser sender, @NonNull ChatUser target, ChatChannel chatChannel, Object value) {
    this.sender = sender;
    this.target = target;
    this.chatChannel = chatChannel;
    this.value = value;
  }

  @Override
  @NonNull
  public ChatUser getSender() {
    return sender;
  }

  @Override
  @NonNull
  public ChatUser getTarget() {
    return target;
  }

  @Override
  public ChatChannel getChatChannel() {
    return chatChannel;
  }

  @Override
  public Object getValue() {
    return value;
  }
}
