package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public class WhisperOptions {

  @Setting(comment = "Plays a sound when receiving private messages with /whisper /msg")
  private @NonNull WhisperPings pings = new WhisperPings();

  @Setting(comment = "")
  private String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";

  @Setting(comment = "")
  private String switchOtherMessage = "<gray><player> <reset><gray>is now in <color><channel> <gray>chat!";

  @Setting(comment = "")
  private String switchFailureMessage = "<red>You cannot use channel <channel>!";

  @Setting(comment = "")
  private String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";

  @Setting(comment = "")
  private String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";

  @Setting(comment = "")
  private String toggleOtherOnMessage = "<gray><player> <reset><gray>can now see <color><channel> <gray>chat!";

  @Setting(comment = "")
  private String toggleOtherOffMessage = "<gray><player> <reset><gray>can no longer see <color><channel> <gray>chat!";

  @Setting(comment = "")
  private String cannotUseMessage = "You cannot use that channel!";

  @Setting(comment = "")
  private String cannotIgnoreMessage = "<red>You cannot ignore that channel!";

  @Setting(comment = "")
  private String senderFormat = "<gold>[<white>Me <gray>-> <white><receiver><gold>] <message>";

  @Setting(comment = "")
  private String receiverFormat = "<gold>[<white><sender> <gray>-> <white>Me<gold>] <message>";

  @Setting(comment = "")
  private String consoleFormat = "<gold>[<white><sender> <gray>-> <white><receiver><gold>] <message>";

  @Setting(comment = "")
  private boolean logToConsole = true;

  public boolean logToConsole() {
    return this.logToConsole;
  }

  public @NonNull WhisperPings pings() {
    return this.pings;
  }

  @Nullable
  public String consoleFormat() {
    return this.consoleFormat;
  }

  @Nullable
  public String senderFormat() {
    return this.senderFormat;
  }

  @Nullable
  public String receiverFormat() {
    return this.receiverFormat;
  }

  @Nullable
  public String switchMessage() {
    return this.switchMessage;
  }

  @Nullable
  public String switchOtherMessage() {
    return this.switchOtherMessage;
  }

  @Nullable
  public String switchFailureMessage() {
    return this.switchFailureMessage;
  }

  @Nullable
  public String cannotIgnoreMessage() {
    return this.cannotIgnoreMessage;
  }

  @Nullable
  public String toggleOffMessage() {
    return this.toggleOffMessage;
  }

  @Nullable
  public String toggleOnMessage() {
    return this.toggleOnMessage;
  }

  @Nullable
  public String toggleOtherOnMessage() {
    return this.toggleOtherOnMessage;
  }

  @Nullable
  public String toggleOtherOffMessage() {
    return this.toggleOtherOffMessage;
  }

  @Nullable
  public String cannotUseMessage() {
    return this.cannotUseMessage;
  }

}
