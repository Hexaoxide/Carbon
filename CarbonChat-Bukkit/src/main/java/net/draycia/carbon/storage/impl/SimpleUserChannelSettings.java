package net.draycia.carbon.storage.impl;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class SimpleUserChannelSettings implements UserChannelSettings {

  @NonNull
  private final transient CarbonChat carbonChat = CarbonChatProvider.carbonChat();
  private boolean spying;
  private boolean ignored;
  @Nullable
  private String color;
  @MonotonicNonNull // @NonNull but not initialised in all constructors.
  private UUID uuid;
  @MonotonicNonNull // @NonNull but not initialised in all constructors.
  private String channel;

  public SimpleUserChannelSettings(final @NonNull UUID uuid, final @NonNull String channel) {
    this.uuid = uuid;
    this.channel = channel;
  }

  @NonNull
  private ChatUser user() {
    return this.carbonChat.userService().wrap(this.uuid);
  }

  @Override
  public boolean spying() {
    return this.spying;
  }

  @Override
  public void spying(final boolean spying, final boolean fromRemote) {
    this.spying = spying;

    if (!fromRemote) {
      this.carbonChat.messageService().sendMessage("spying-channel", this.uuid, byteArray -> {
        byteArray.writeUTF(this.channel);
        byteArray.writeBoolean(spying);
      });
    }
  }

  @Override
  public boolean ignored() {
    return this.ignored;
  }

  @Override
  public void ignoring(final boolean ignored, final boolean fromRemote) {
    this.ignored = ignored;

    if (!fromRemote) {
      this.carbonChat.messageService().sendMessage("ignoring-channel", this.uuid, byteArray -> {
        byteArray.writeUTF(this.channel);
        byteArray.writeBoolean(ignored);
      });
    }
  }

  @Override
  @Nullable
  public TextColor color() {
    if (this.color == null) {
      return null;
    }

    return TextColor.fromHexString(this.color);
  }

  @Override
  public void color(@Nullable final TextColor color, final boolean fromRemote) {
    if (color == null) {
      this.color = null;
    } else {
      this.color = color.asHexString();
    }

    if (!fromRemote) {
      this.carbonChat.messageService().sendMessage("channel-color", this.uuid, byteArray -> {
        byteArray.writeUTF(color.asHexString());
      });
    }
  }

}
