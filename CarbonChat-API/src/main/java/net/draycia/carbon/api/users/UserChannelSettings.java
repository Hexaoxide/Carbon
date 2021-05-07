package net.draycia.carbon.api.users;

import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserChannelSettings {

  boolean spying();

  void spying(boolean spying);

  boolean ignored();

  void ignoring(boolean ignored);

  @Nullable
  TextColor color();

  void color(@Nullable TextColor color);

}
