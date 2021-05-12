package net.draycia.carbon.api.events;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.PlayerComponentRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import static net.kyori.adventure.text.Component.translatable;

/**
 * {@link CancellableCarbonEvent} that's called when chat components are rendered for online players.
 */
public class CarbonChatEvent extends CancellableCarbonEvent {

  private @NonNull PlayerComponentRenderer renderer = (sender, recipient, message) -> {
    return translatable("chat.type.text", sender.displayName(), message);
  };

  private final @NonNull List<@NonNull CarbonPlayer> recipients;

  public CarbonChatEvent(final @NonNull List<@NonNull CarbonPlayer> recipients) {
    this.recipients = recipients;
  }

  public void renderer(final @NonNull PlayerComponentRenderer renderer) {
    this.renderer = renderer;
  }

  /**
   * @return The per-player component renderer.
   */
  public @NonNull PlayerComponentRenderer renderer() {
    return this.renderer;
  }

  /**
   * The players that will receive the message.
   * List is mutable and players may be added/removed.
   * @return The players that will receive this message.
   */
  public @NonNull List<@NonNull CarbonPlayer> recipients() {
    return this.recipients;
  }

}
