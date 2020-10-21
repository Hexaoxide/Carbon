package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
public class Context {

  @Setting private final @NonNull String key;
  @Setting private final @NonNull Object value;

  public Context(final @NonNull String key, final @NonNull Object value) {
    this.key = key;
    this.value = value;
  }

  public @NonNull String key() {
    return this.key;
  }

  public @NonNull Object value() {
    return this.value;
  }

  public boolean isFloat() {
    return this.value instanceof Float;
  }

  public boolean isDouble() {
    return this.value() instanceof Double;
  }

  public boolean isByte() {
    return this.value() instanceof Byte;
  }

  public boolean isInteger() {
    return this.value() instanceof Integer;
  }

  public boolean isLong() {
    return this.value() instanceof Long;
  }

  public boolean isString() {
    return this.value() instanceof String;
  }

  public boolean isBoolean() {
    return this.value() instanceof Boolean;
  }

  public boolean isList() {
    return this.value() instanceof List;
  }

  public boolean isListChecked(final Class<?> type) {
    if (!this.isList()) {
      return false;
    }

    final List<?> list = this.asList();

    if (list.size() > 0) {
      final Object value = list.get(0);

      if (value != null) {
        return list.get(0).getClass().isAssignableFrom(type);
      }
    }

    return false;
  }

  public boolean isNumber() {
    return this.value() instanceof Number;
  }

  public String asString() {
    return (String) this.value();
  }

  public Boolean asBoolean() {
    return (Boolean) this.value();
  }

  public <T> List<T> asList() {
    return (List<T>) this.value();
  }

  public Number asNumber() {
    return (Number) this.value();
  }

}
