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

  public SimpleMessageContext(@NonNull final ChatUser sender, @NonNull final ChatUser target,
                              @NonNull final ChatChannel chatChannel, @NonNull final Object value) {
    this.sender = sender;
    this.target = target;
    this.chatChannel = chatChannel;
    this.value = value;
  }

  @Override
  @NonNull
  public ChatUser sender() {
    return this.sender;
  }

  @Override
  @NonNull
  public ChatUser target() {
    return this.target;
  }

  @Override
  public ChatChannel chatChannel() {
    return this.chatChannel;
  }

  @Override
  public Object value() {
    return this.value;
  }
  
}
