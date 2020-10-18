package net.draycia.carbon.api.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigSerializable
public final class ModerationSettings {

  private static final ObjectMapper<ModerationSettings> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.factory().get(ModerationSettings.class);
    } catch (final SerializationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static ModerationSettings loadFrom(final CommentedConfigurationNode node) throws SerializationException {
    return MAPPER.load(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws SerializationException {
    MAPPER.save(this, node);
  }

  @Setting
  @Comment("The text that's prepended to someone else's shadowmuted messages.")
  private String shadowMutePrefix = "[SM] ";

  public String shadowMutePrefix() {
    return this.shadowMutePrefix;
  }

  @Setting
  @Comment("If true, muted users will be unable to use /whisper /msg")
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
    @Setting
  @Comment("The message sent to clear chat, you probably want to leave this blank")
    private String message = "";

    @Setting
  @Comment("How many messages will be sent in order to clear chat")
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
    @Setting
    @Comment("If caps protection is enabled")
    private boolean enabled = true;

    @Setting
    @Comment("The minimum message length for caps protection to activate")
    private int minimumLength = 10;

    @Setting
    @Comment("The amount of letters in the message for it to trigger the protection")
    private float percentCaps = 0.80F;

    @Setting
    @Comment("If true, stops message from sending. If false, simply changes the message to lowercase")
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

  @Setting
  private Filters filters = new Filters();

  public Filters filters() {
    return this.filters;
  }

  @ConfigSerializable
  public static class Filters {
    @Setting
    private boolean enabled = true;

    @Setting
    @Comment("The keys (\"****\" for example) are what the text is replaced with.\n" +
      "The strings in the lists (\"lag\" etc) are what's replaced.\n" +
      "Set to filters: {} if you want to disable the filter feature.\n" +
      "Set the key to \"_\" for the replacement to be blank (remove the filtered pattern).")
    private Map<String, List<Pattern>> replacements = new HashMap<String, List<Pattern>>() {
      {
        this.put("****", Collections.singletonList(Pattern.compile("la[g]+")));
      }
    };

    @Setting
    @Comment("Anything in blocked-words will prevent the message from being sent at all.\n" +
      "Set to blocked-words: [] if you want to disable the blocked words feature.")
    private List<Pattern> blockedPatterns = Collections.singletonList(Pattern.compile("pineapple doesn't belong on pizza"));

    public boolean enabled() {
      return this.enabled;
    }

    public Map<String, List<Pattern>> replacements() {
      return this.replacements;
    }

    public List<Pattern> blockedPatterns() {
      return this.blockedPatterns;
    }
  }

}
