package net.draycia.carbon.api.events;

import net.kyori.event.Cancellable;

/**
 * Represents a {@link CarbonEvent} that's cancellable.
 *
 * @since 2.0.0
 */
public abstract class CancellableCarbonEvent extends Cancellable.Impl implements CarbonEvent {

}
