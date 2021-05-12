package net.draycia.carbon.api.events;

import net.kyori.event.Cancellable;

/**
 * Represents a {@link CarbonEvent} that's cancellable.
 */
public abstract class CancellableCarbonEvent extends Cancellable.Impl implements CarbonEvent {
}
