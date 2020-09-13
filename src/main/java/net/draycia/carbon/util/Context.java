package net.draycia.carbon.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class Context {

  private @NonNull final String key;
  private @NonNull final Object value;

  public Context(@NonNull final String key, @NonNull final Object value) {
    this.key = key;
    this.value = value;
  }

  public @NonNull String key() {
    return this.key;
  }

  public @NonNull Object value() {
    return this.value;
  }

  public boolean isDouble() {
    return this.value() instanceof Double;
  }

  public boolean isString() {
    return this.value() instanceof String;
  }

  public boolean isInteger() {
    return this.value() instanceof Integer;
  }

  public boolean isBoolean() {
    return this.value() instanceof Boolean;
  }

  public boolean isList() {
    return this.value() instanceof List;
  }

  public Double asDouble() {
    return (Double) this.value();
  }

  public String asString() {
    return (String) this.value();
  }

  public Integer asInteger() {
    return (Integer) this.value();
  }

  public Boolean asBoolean() {
    return (Boolean) this.value();
  }

  public List<?> asList() {
    return (List<?>) this.value();
  }

  public List<String> asStringList() {
    return (List<String>) this.value();
  }

}
