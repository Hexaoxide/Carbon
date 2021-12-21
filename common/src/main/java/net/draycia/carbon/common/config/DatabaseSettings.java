package net.draycia.carbon.common.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@DefaultQualifier(Nullable.class)
@ConfigSerializable
public class DatabaseSettings {

    @Comment("JDBC URL")
    private String url = "jdbc:mysql://localhost:3306/carbon";

    @Comment("")
    private String username = "username";

    @Comment("")
    private String password = "password";

    public String url() {
        return this.url;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

}
