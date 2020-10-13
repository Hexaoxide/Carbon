package net.draycia.carbon.api.events;

import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.CarbonUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UserEvent implements CarbonEvent, Cancellable {

  private @NonNull final CarbonUser user;
  private boolean cancelled = false;

  public UserEvent(@NonNull final CarbonUser user) {
    this.user = user;
  }

  public @NonNull CarbonUser user() {
    return this.user;
  }

  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  public static class Join extends UserEvent {
    public Join(@NonNull final CarbonUser user) {
      super(user);
    }
  }

  public static class Leave extends UserEvent {
    public Leave(@NonNull final CarbonUser user) {
      super(user);
    }
  }

}
