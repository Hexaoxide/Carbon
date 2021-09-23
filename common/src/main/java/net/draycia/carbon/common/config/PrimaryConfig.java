package net.draycia.carbon.common.config;

import java.util.Locale;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PrimaryConfig {

    @Comment("The default locale for plugin messages.")
    private final Locale defaultLocale = Locale.US;
    @Comment("The default channel that new players will be in when they join.")
    private final Key defaultChannel = Key.key("carbon", "basic");
    @Comment("The service that will be used to store and load player information.")
    private final StorageType storageType = StorageType.JSON;
    @Comment("Should we hide join/quit and death messages of muted players?")
    private final boolean hideMutedJoinLeaveQuit = false;

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

    public enum StorageType {
        JSON,
        MYSQL,
        PSQL
    }

}
