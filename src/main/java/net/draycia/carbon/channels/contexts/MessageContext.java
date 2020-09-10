package net.draycia.carbon.channels.contexts;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MessageContext {

  @NonNull
  ChatUser sender();

  @NonNull
  ChatUser target();

  ChatChannel chatChannel();

  Object value();

  default boolean isDouble() {
    return this.value() instanceof Double;
  }

  default boolean isString() {
    return this.value() instanceof String;
  }

  default boolean isInteger() {
    return this.value() instanceof Integer;
  }

  default boolean isBoolean() {
    return this.value() instanceof Boolean;
  }

  default Double asDouble() {
    return (Double) this.value();
  }

  default String asString() {
    return (String) this.value();
  }

  default Integer asInteger() {
    return (Integer) this.value();
  }

  default Boolean asBoolean() {
    return (Boolean) this.value();
  }

}
