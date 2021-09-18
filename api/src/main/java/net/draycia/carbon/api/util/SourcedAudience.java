package net.draycia.carbon.api.util;

import net.kyori.adventure.audience.Audience;

public record SourcedAudience(Audience sender, Audience recipient) {

    public static SourcedAudience empty() {
        return new SourcedAudience(Audience.empty(), Audience.empty());
    }

}
