package net.draycia.carbon.common.config;

import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PrimaryConfig {

    @Comment("The default locale for plugin messages.")
    private Locale defaultLocale = Locale.US;

    @Comment("""
    The default channel that new players will be in when they join.
    If the channel is not found or the player cannot use the channel, they will speak in basic non-channel chat.
    """)
    private Key defaultChannel = Key.key("carbon", "basic");

    @Comment("The service that will be used to store and load player information.")
    private StorageType storageType = StorageType.JSON;

    @Comment("Should we hide join/quit and death messages of muted players?")
    private boolean hideMutedJoinLeaveQuit = false;

    @Comment("Various ClearChat command settings.")
    private ClearChatSettings clearChatSettings = new ClearChatSettings();

    @Comment("""
    Plugin-wide custom placeholders.
    These will be parsed in all messages rendered and sent by Carbon.
    This includes chat, command feedback, and others.
    Make sure to close your tags so they do not bleed into other formats.
    Only a single pass is done so custom placeholders will not work within each other.
    """)
    private Map<String, String> customPlaceholders = Map.of();

    public Locale defaultLocale() {
        return this.defaultLocale;
    }

    public Key defaultChannel() {
        return this.defaultChannel;
    }

    public StorageType storageType() {
        return this.storageType;
    }

    public boolean hideMutedJoinLeaveQuit() {
        return this.hideMutedJoinLeaveQuit;
    }

    public ClearChatSettings clearChatSettings() {
        return this.clearChatSettings;
    }

    public Map<String, String> customPlaceholders() {
        return this.customPlaceholders;
    }

    public enum StorageType {
        JSON,
        MYSQL,
        PSQL
    }

}
