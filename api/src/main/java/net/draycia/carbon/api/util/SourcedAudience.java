package net.draycia.carbon.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

public record SourcedAudience(Audience sender, Audience recipient) implements ForwardingAudience.Single {

    public static final SourcedAudience EMPTY = new SourcedAudience(Audience.empty(), Audience.empty());

    public static SourcedAudience empty() {
        return EMPTY;
    }

    @Override
    public @NotNull Audience audience() {
        return Audience.audience(sender, recipient);
    }

}
