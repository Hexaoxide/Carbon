package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class WhisperOptions {

  @Setting
  @Comment("Plays a sound when receiving private messages with /whisper /msg")
  private @NonNull WhisperPings pings = new WhisperPings();

  @Setting
  @Comment("")
  private String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private String switchOtherMessage = "<gray><player> <reset><gray>is now in <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private String switchFailureMessage = "<red>You cannot use channel <channel>!";

  @Setting
  @Comment("")
  private String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private String toggleOtherOnMessage = "<gray><player> <reset><gray>can now see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private String toggleOtherOffMessage = "<gray><player> <reset><gray>can no longer see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private String cannotUseMessage = "You cannot use that channel!";

  @Setting
  @Comment("")
  private String cannotIgnoreMessage = "<red>You cannot ignore that channel!";

  @Setting
  @Comment("")
  private String senderFormat = "<gold>[<white>Me <gray>-> <white><receiver><gold>] <message>";

  @Setting
  @Comment("")
  private String receiverFormat = "<gold>[<white><sender> <gray>-> <white>Me<gold>] <message>";

  @Setting
  @Comment("")
  private String consoleFormat = "<gold>[<white><sender> <gray>-> <white><receiver><gold>] <message>";

  @Setting
  @Comment("")
  private String nowWhisperingPlayer = "<white>You are now whispering <green><player><white>!";

  @Setting
  @Comment("")
  private boolean logToConsole = true;

  public boolean logToConsole() {
    return this.logToConsole;
  }

  public @NonNull WhisperPings pings() {
    return this.pings;
  }

  public @Nullable String consoleFormat() {
    return this.consoleFormat;
  }

  public @Nullable String senderFormat() {
    return this.senderFormat;
  }

  public @Nullable String receiverFormat() {
    return this.receiverFormat;
  }

  public @Nullable String switchMessage() {
    return this.switchMessage;
  }

  public @Nullable String switchOtherMessage() {
    return this.switchOtherMessage;
  }

  public @Nullable String switchFailureMessage() {
    return this.switchFailureMessage;
  }

  public @Nullable String cannotIgnoreMessage() {
    return this.cannotIgnoreMessage;
  }

  public @Nullable String toggleOffMessage() {
    return this.toggleOffMessage;
  }

  public @Nullable String toggleOnMessage() {
    return this.toggleOnMessage;
  }

  public @Nullable String toggleOtherOnMessage() {
    return this.toggleOtherOnMessage;
  }

  public @Nullable String toggleOtherOffMessage() {
    return this.toggleOtherOffMessage;
  }

  public @Nullable String cannotUseMessage() {
    return this.cannotUseMessage;
  }

  public @Nullable String nowWhisperingPlayer() {
    return this.nowWhisperingPlayer;
  }

}
