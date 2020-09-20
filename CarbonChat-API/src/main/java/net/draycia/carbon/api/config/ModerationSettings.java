package net.draycia.carbon.api.config;

import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public class ModerationSettings {

  // TODO: comment this
  @Setting
  private String shadowMutePrefix = "[SM] ";

  public String shadowMutePrefix() {
    return this.shadowMutePrefix;
  }

  @Setting(comment = "If true, muted users will be unable to use /whisper /msg")
  private boolean muteStopsWhispers = true;

  public boolean muteStopsWhispers() {
    return this.muteStopsWhispers;
  }

  @Setting
  private ClearChat clearChat = new ClearChat();

  public ClearChat clearChat() {
    return this.clearChat;
  }

  @ConfigSerializable
  public static class ClearChat {
    @Setting(comment = "The message sent to clear chat, you probably want to leave this blank")
    private String message = "";

    @Setting(comment = "How many messages will be sent in order to clear chat")
    private int messageCount = 100;

    public String message() {
      return this.message;
    }

    public int messageCount() {
      return this.messageCount;
    }
  }

  @Setting
  private CapsProtection capsProtection = new CapsProtection();

  public CapsProtection capsProtection() {
    return this.capsProtection;
  }

  @ConfigSerializable
  public static class CapsProtection {
    @Setting(comment = "If caps protection is enabled")
    private boolean enabled = true;

    @Setting(comment = "The minimum message length for caps protection to activate")
    private int minimumLength = 10;

    @Setting(comment = "The amount of letters in the message for it to trigger the protection")
    private float percentCaps = 0.80F;

    @Setting(comment = "If true, stops message from sending. If false, simply changes the message to lowercase")
    private boolean blockMessage = false;

    public boolean enabled() {
      return this.enabled;
    }

    public int minimumLength() {
      return this.minimumLength;
    }

    public float percentCaps() {
      return this.percentCaps;
    }

    public boolean blockMessage() {
      return this.blockMessage;
    }
  }

}
