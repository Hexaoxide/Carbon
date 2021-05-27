package net.draycia.carbon.common.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PrimaryConfig {

    @Comment("The default locale for plugin messages.")
    private String defaultLocale = "en_US";

    public String defaultLocale() {
        return this.defaultLocale;
    }

}
