package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class WhisperOptions {

  @Setting
  @Comment("Plays a sound when receiving private messages with /whisper /msg")
  private @NonNull WhisperPings pings = new WhisperPings();

  @Setting
  @Comment("")
  private @NonNull String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private @NonNull String switchOtherMessage = "<gray><player> <reset><gray>is now in <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private @NonNull String switchFailureMessage = "<red>You cannot use channel <channel>!";

  @Setting
  @Comment("")
  private @NonNull String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private @NonNull String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private @NonNull String toggleOtherOnMessage = "<gray><player> <reset><gray>can now see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private @NonNull String toggleOtherOffMessage = "<gray><player> <reset><gray>can no longer see <color><channel> <gray>chat!";

  @Setting
  @Comment("")
  private @NonNull String cannotUseMessage = "You cannot use that channel!";

  @Setting
  @Comment("")
  private @NonNull String cannotIgnoreMessage = "<red>You cannot ignore that channel!";

  @Setting
  @Comment("")
  private @NonNull String senderFormat = "<gold>[<white>Me <gray>-> <white><receiver><gold>] <message>";

  @Setting
  @Comment("")
  private @NonNull String receiverFormat = "<gold>[<white><sender> <gray>-> <white>Me<gold>] <message>";

  @Setting
  @Comment("")
  private @NonNull String consoleFormat = "<gold>[<white><sender> <gray>-> <white><receiver><gold>] <message>";

  @Setting
  @Comment("")
  private @NonNull String nowWhisperingPlayer = "<white>You are now whispering <green><player><white>!";

  @Setting
  @Comment("The message that's sent when attempting to message someone when you have whispers toggled off.")
  private @NonNull String senderToggledOff = "<red>You have whispers toggled off!";

  @Setting
  @Comment("The message that's sent when attempting to message someone who has whispers toggled off.")
  private @NonNull String receiverToggledOff = "<red><receiver> <red>has whispers toggled off!";

  @Setting
  @Comment("")
  private boolean logToConsole = true;

  public boolean logToConsole() {
    return this.logToConsole;
  }

  public @NonNull WhisperPings pings() {
    return this.pings;
  }

  public @NonNull String consoleFormat() {
    return this.consoleFormat;
  }

  public @NonNull String senderFormat() {
    return this.senderFormat;
  }

  public @NonNull String receiverFormat() {
    return this.receiverFormat;
  }

  public @NonNull String switchMessage() {
    return this.switchMessage;
  }

  public @NonNull String switchOtherMessage() {
    return this.switchOtherMessage;
  }

  public @NonNull String switchFailureMessage() {
    return this.switchFailureMessage;
  }

  public @NonNull String cannotIgnoreMessage() {
    return this.cannotIgnoreMessage;
  }

  public @NonNull String toggleOffMessage() {
    return this.toggleOffMessage;
  }

  public @NonNull String toggleOnMessage() {
    return this.toggleOnMessage;
  }

  public @NonNull String toggleOtherOnMessage() {
    return this.toggleOtherOnMessage;
  }

  public @NonNull String toggleOtherOffMessage() {
    return this.toggleOtherOffMessage;
  }

  public @NonNull String cannotUseMessage() {
    return this.cannotUseMessage;
  }

  public @NonNull String nowWhisperingPlayer() {
    return this.nowWhisperingPlayer;
  }

  public @NonNull String senderToggledOff() {
    return this.senderToggledOff;
  }

  public @NonNull String receiverToggledOff() {
    return this.receiverToggledOff;
  }

}
