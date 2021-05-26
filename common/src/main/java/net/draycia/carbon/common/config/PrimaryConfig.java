package net.draycia.carbon.common.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PrimaryConfig {

    @Comment("""
            The file that localisations are loaded from.
            Files will be pulled from the jar.
            """)
    private String translationFile = "locale/messages.properties";

    public String translationFile() {
        return this.translationFile;
    }

}
