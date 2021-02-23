package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class Context {

  @Setting private final @NonNull String key;
  @Setting private final @NonNull String value;

  public Context(final @NonNull String key, final @NonNull String value) {
    this.key = key;
    this.value = value;
  }

  public @NonNull String key() {
    return this.key;
  }

  public @NonNull String value() {
    return this.value;
  }

  public boolean isFloat() {
    try {
      Float.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public boolean isDouble() {
    try {
      Double.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public boolean isByte() {
    try {
      Byte.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public boolean isInteger() {
    try {
      Integer.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public boolean isLong() {
    try {
      Long.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public boolean isString() {
    return true;
  }

  public boolean isBoolean() {
    try {
      Boolean.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public boolean isList() {
    return true;
  }

  public boolean isListChecked(final Class<?> type) {
    final List<?> list = this.asList();

    if (list.size() > 0) {
      final Object value = list.get(0);

      if (value != null) {
        return value.getClass().isAssignableFrom(type);
      }
    }

    return false;
  }

  public boolean isNumber() {
    try {
      Double.valueOf(this.value);
    } catch (final NumberFormatException ignored) {
      return false;
    }

    return true;
  }

  public @NonNull String asString() {
    return this.value();
  }

  public @NonNull Boolean asBoolean() {
    return Boolean.valueOf(this.value());
  }

  public @NonNull List<String> asList() {
    return Arrays.asList(this.value.split(","));
  }

  public @Nullable Number asNumber() {
    try {
      return Double.valueOf(this.value());
    } catch (final NumberFormatException exception) {
      return null;
    }
  }

}
