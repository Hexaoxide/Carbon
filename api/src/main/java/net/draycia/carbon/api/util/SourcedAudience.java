package net.draycia.carbon.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

/**
 * An audience, where messages are sent from another Audience.
 *
 * @since 2.0.0
 */
public record SourcedAudience(Audience sender, Audience recipient) implements ForwardingAudience.Single {

    public static final SourcedAudience EMPTY = new SourcedAudience(Audience.empty(), Audience.empty());

    /**
     * An empty {@link SourcedAudience}, with an empty sender and recipient.
     *
     * @return an empty sourced audience
     * @since 2.0.0
     */
    public static SourcedAudience empty() {
        return EMPTY;
    }

    @Override
    public @NotNull Audience audience() {
        return Audience.audience(this.sender, this.recipient);
    }

}
