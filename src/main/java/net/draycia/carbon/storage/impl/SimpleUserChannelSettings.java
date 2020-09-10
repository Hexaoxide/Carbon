package net.draycia.carbon.storage.impl;

import java.util.UUID;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.UserChannelSettings;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimpleUserChannelSettings implements UserChannelSettings {

  @NonNull private final transient CarbonChat carbonChat;
  private boolean spying;
  private boolean ignored;
  @Nullable private String color;
  @MonotonicNonNull // @NonNull but not initialised in all constructors.
  private UUID uuid;
  @MonotonicNonNull // @NonNull but not initialised in all constructors.
  private String channel;

  private SimpleUserChannelSettings() {
    carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");
  }

  public SimpleUserChannelSettings(@NonNull UUID uuid, @NonNull String channel) {
    this.uuid = uuid;
    this.channel = channel;

    carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");
  }

  @NonNull
  private CarbonChatUser getUser() {
    return (CarbonChatUser) carbonChat.getUserService().wrap(uuid);
  }

  @Override
  public boolean isSpying() {
    return this.spying;
  }

  @Override
  public void setSpying(boolean spying, boolean fromRemote) {
    this.spying = spying;

    if (!fromRemote) {
      carbonChat
          .getMessageManager()
          .sendMessage(
              "spying-channel",
              uuid,
              (byteArray) -> {
                byteArray.writeUTF(channel);
                byteArray.writeBoolean(spying);
              });
    }
  }

  @Override
  public boolean isIgnored() {
    return ignored;
  }

  @Override
  public void setIgnoring(boolean ignored, boolean fromRemote) {
    this.ignored = ignored;

    if (!fromRemote) {
      carbonChat
          .getMessageManager()
          .sendMessage(
              "ignoring-channel",
              uuid,
              (byteArray) -> {
                byteArray.writeUTF(channel);
                byteArray.writeBoolean(ignored);
              });
    }
  }

  @Override
  @Nullable
  public TextColor getColor() {
    if (color == null) {
      return null;
    }

    return TextColor.fromHexString(color);
  }

  @Override
  public void setColor(@Nullable TextColor color, boolean fromRemote) {
    if (color == null) {
      this.color = null;
    } else {
      this.color = color.asHexString();
    }

    if (!fromRemote) {
      carbonChat
          .getMessageManager()
          .sendMessage(
              "channel-color",
              uuid,
              (byteArray) -> {
                byteArray.writeUTF(color.asHexString());
              });
    }
  }
}
