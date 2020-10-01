package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public class WhisperOptions {

  @Setting(comment = "")
  private String switchMessage;

  @Setting(comment = "")
  private String switchOtherMessage;

  @Setting(comment = "")
  private String switchFailureMessage;

  @Setting(comment = "")
  private String toggleOnMessage;

  @Setting(comment = "")
  private String toggleOffMessage;

  @Setting(comment = "")
  private String toggleOtherOnMessage;

  @Setting(comment = "")
  private String toggleOtherOffMessage;

  @Setting(comment = "")
  private String cannotUseMessage;

  @Setting(comment = "")
  private String cannotIgnoreMessage;

  @Setting(comment = "")
  private String senderFormat;

  @Setting(comment = "")
  private String receiverFormat;

  @Setting(comment = "")
  private String consoleFormat;

  @Setting(comment = "")
  private boolean logToConsole;

  public boolean logToConsole() {
    return this.logToConsole;
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
