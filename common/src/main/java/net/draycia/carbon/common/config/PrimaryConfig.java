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
    private Locale defaultLocale = Locale.US;

    public Locale defaultLocale() {
        return this.defaultLocale;
    }

    @Comment("The default channel that new players will be in when they join.")
    private Key defaultChannel = Key.key("carbon", "basic");

    public Key defaultChannel() {
        return this.defaultChannel;
    }

    @Comment("The service that will be used to store and load player information.")
    private StorageType storageType = StorageType.JSON;

    public StorageType storageType() {
        return this.storageType;
    }

    public enum StorageType {
        JSON,
        MYSQL,
        PSQL
    }

}
