package net.draycia.carbon.channels.contexts;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MessageContext {

  @NonNull
  ChatUser getSender();

  @NonNull
  ChatUser getTarget();

  ChatChannel getChatChannel();

  Object getValue();

  default boolean isDouble() {
    return getValue() instanceof Double;
  }

  default boolean isString() {
    return getValue() instanceof String;
  }

  default boolean isInteger() {
    return getValue() instanceof Integer;
  }

  default boolean isBoolean() {
    return getValue() instanceof Boolean;
  }

  default Double asDouble() {
    return (Double) getValue();
  }

  default String asString() {
    return (String) getValue();
  }

  default Integer asInteger() {
    return (Integer) getValue();
  }

  default Boolean asBoolean() {
    return (Boolean) getValue();
  }

}
