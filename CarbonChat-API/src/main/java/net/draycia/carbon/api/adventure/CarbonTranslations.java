package net.draycia.carbon.api.adventure;

import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public class CarbonTranslations {

  private static final ObjectMapper<CarbonTranslations> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.forClass(CarbonTranslations.class); // We hold on to the instance of our ObjectMapper
    } catch (final ObjectMappingException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static CarbonTranslations loadFrom(final BasicConfigurationNode node) throws ObjectMappingException {
    return MAPPER.bindToNew().populate(node);
  }

  public void saveTo(final BasicConfigurationNode node) throws ObjectMappingException {
    MAPPER.bind(this).serialize(node);
  }

  @Setting private String reloaded = "<yellow>Chat config has been reloaded!";
  @Setting private String messageToOther = "<green>[<gray>Me <gold>-> <gray><target><reset><green>] <message>";
  @Setting private String messageFromOther = "<green>[<gray><sender><reset> <gold>-> <gray>Me<green>] <message>";
  @Setting private String otherPlayerOffline = "<red>That player is offline!";
  @Setting private String spyWhispers = "<yellow>Spy [<gray><sender><reset> <gold>-> <gray><target><reset><yellow>] <message>";
  @Setting private String noReplyTarget = "<red>You have no one to reply to!";
  @Setting private String otherNicknameSet = "<green><user><reset>''s nickname was set to <nickname><green>!";
  @Setting private String otherNicknameReset = "<green><user><reset>'s nickname was reset!";
  @Setting private String availableChannelsList = "<green>Available Channels:<reset> <list>.";
  @Setting private String unavailableChannelsList = "<red>Unavailable Channels:<reset> <list>.";
  @Setting private String channelListSeparator = ", ";
  @Setting private String userMustBeOnline = "<red><player> must be online!";
  @Setting private String replyMessageBlank = "<red>Your reply message was blank!";
  @Setting private String invalidColor = "<red>The input <input> is not a valid color!";
  @Setting private String cannotSetColor = "<red>You do not have permission to set a color for this channel!";
  @Setting private String roleplayFormat = "<italic><dark_purple>*<displayname> <reset><message>*</dark_purple></italic>";
  @Setting private String ignoringUser = "<green>You are now ignoring <gold><player><reset><green>!";
  @Setting private String notIgnoringUser = "<green>You are no longer ignoring <gold><player><reset><green>!";
  @Setting private String ignoreExempt = "<red>User <player> <red>is exempt from being ignored!";
  @Setting private String channelColorSet = "<green>You have set <color><channel> <green>color to <hex>!";
  @Setting private String spyToggledOn = "<gray>You are now spying on <color><channel> <gray>chat!";
  @Setting private String spyToggledOff = "<gray>You are no longer spying on <color><channel> <gray>chat!";
  @Setting private String spyWhispersOn = "<gray>You are now spying on whispers!";
  @Setting private String spyWhispersOff = "<gray>You are no longer spying on whispers!";
  @Setting private String spyEverythingOn = "<gray>All spying toggled on!";
  @Setting private String spyEverythingOff = "<gray>All spying toggled off!";
  @Setting private String nicknameSet = "<green>Your nickname was set to <nickname><reset><green>!";
  @Setting private String nicknameReset = "<green>Your nickname was reset!";
  @Setting private String nowShadowMuted = "<yellow>Player <player> is now shadow muted!";
  @Setting private String noLongerShadowMuted = "<yellow>Player <player> is no longer shadow muted!";
  @Setting private String shadowMuteExempt = "<red>Player <player> is exempt from being shadow muted!";
  @Setting private String nowMuted = "<yellow>Player <player> is now muted!";
  @Setting private String noLongerMuted = "<yellow>Player <player> is no longer muted!";
  @Setting private String muteExempt = "<red>Player <player> is exempt from being muted!";
  @Setting private String clearNotify = "<yellow>Chat has been cleared by <player>.";
  @Setting private String clearExempt = "<yellow>You were exempt from the clear due to your permissions!";

  public String reloaded() {
    return this.reloaded;
  }

  public String messageToOther() {
    return this.messageToOther;
  }

  public String messageFromOther() {
    return this.messageFromOther;
  }

  public String otherPlayerOffline() {
    return this.otherPlayerOffline;
  }

  public String spyWhispers() {
    return this.spyWhispers;
  }

  public String noReplyTarget() {
    return this.noReplyTarget;
  }

  public String otherNicknameSet() {
    return this.otherNicknameSet;
  }

  public String otherNicknameReset() {
    return this.otherNicknameReset;
  }

  public String availableChannelsList() {
    return this.availableChannelsList;
  }

  public String unavailableChannelsList() {
    return this.unavailableChannelsList;
  }

  public String channelListSeparator() {
    return this.channelListSeparator;
  }

  public String userMustBeOnline() {
    return this.userMustBeOnline;
  }

  public String replyMessageBlank() {
    return this.replyMessageBlank;
  }

  public String invalidColor() {
    return this.invalidColor;
  }

  public String cannotSetColor() {
    return this.cannotSetColor;
  }

  public String roleplayFormat() {
    return this.roleplayFormat;
  }

  public String ignoringUser() {
    return this.ignoringUser;
  }

  public String notIgnoringUser() {
    return this.notIgnoringUser;
  }

  public String ignoreExempt() {
    return this.ignoreExempt;
  }

  public String channelColorSet() {
    return this.channelColorSet;
  }

  public String spyToggledOn() {
    return this.spyToggledOn;
  }

  public String spyToggledOff() {
    return this.spyToggledOff;
  }

  public String spyWhispersOn() {
    return this.spyWhispersOn;
  }

  public String spyWhispersOff() {
    return this.spyWhispersOff;
  }

  public String spyEverythingOn() {
    return this.spyEverythingOn;
  }

  public String spyEverythingOff() {
    return this.spyEverythingOff;
  }

  public String nicknameSet() {
    return this.nicknameSet;
  }

  public String nicknameReset() {
    return this.nicknameReset;
  }

  public String nowShadowMuted() {
    return this.nowShadowMuted;
  }

  public String noLongerShadowMuted() {
    return this.noLongerShadowMuted;
  }

  public String shadowMuteExempt() {
    return this.shadowMuteExempt;
  }

  public String nowMuted() {
    return this.nowMuted;
  }

  public String noLongerMuted() {
    return this.noLongerMuted;
  }

  public String muteExempt() {
    return this.muteExempt;
  }

  public String clearNotify() {
    return this.clearNotify;
  }

  public String clearExempt() {
    return this.clearExempt;
  }

}
