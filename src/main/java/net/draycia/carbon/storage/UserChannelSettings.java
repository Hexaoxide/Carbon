package net.draycia.carbon.storage;

import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserChannelSettings {

  boolean isSpying();

  default void setSpying(boolean spying) {
    this.setSpying(spying, false);
  }

  void setSpying(boolean spying, boolean fromRemote);

  boolean isIgnored();

  void setIgnoring(boolean ignored, boolean fromRemote);

  default void setIgnoring(boolean ignored) {
    this.setIgnoring(ignored, false);
  }

  @Nullable
  TextColor getColor();

  default void setColor(@Nullable TextColor color) {
    this.setColor(color, false);
  }

  void setColor(@Nullable TextColor color, boolean fromRemote);
}
