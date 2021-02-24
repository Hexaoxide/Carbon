package net.draycia.carbon.api.events;

import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UserEvent extends Cancellable.Impl implements CarbonEvent {

  private final @NonNull PlayerUser user;

  public UserEvent(final @NonNull PlayerUser user) {
    this.user = user;
  }

  public @NonNull PlayerUser user() {
    return this.user;
  }

  public static class Join extends UserEvent {
    public Join(final @NonNull PlayerUser user) {
      super(user);
    }
  }

  public static class Leave extends UserEvent {
    public Leave(final @NonNull PlayerUser user) {
      super(user);
    }
  }

}
