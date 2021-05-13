package net.draycia.carbon.api.events;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.PlayerComponentRenderer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import static net.kyori.adventure.text.Component.translatable;

/**
 * {@link CancellableCarbonEvent} that's called when chat components are rendered for online players.
 */
public class CarbonChatEvent extends CancellableCarbonEvent {

  private @NonNull PlayerComponentRenderer renderer = (sender, recipient, message) ->
    translatable("chat.type.text", sender.displayName(), message);

  private final @NonNull CarbonPlayer sender;

  private final @NonNull Component originalMessage;

  private @NonNull Component message;

  private final @NonNull List<@NonNull CarbonPlayer> recipients;

  public CarbonChatEvent(
    final @NonNull CarbonPlayer sender,
    final @NonNull Component originalMessage,
    final @NonNull List<@NonNull CarbonPlayer> recipients
  ) {
    this.sender = sender;
    this.originalMessage = originalMessage;
    this.message = originalMessage;
    this.recipients = recipients;
  }

  /**
   * Set the {@link PlayerComponentRenderer} for the event.
   *
   * @param renderer new {@link PlayerComponentRenderer}
   */
  public void renderer(final @NonNull PlayerComponentRenderer renderer) {
    this.renderer = renderer;
  }

  /**
   * Get the renderer used to construct components for each player in recipients.
   *
   * @return The per-player component renderer.
   */
  public @NonNull PlayerComponentRenderer renderer() {
    return this.renderer;
  }

  /**
   * Get the sender of the message.
   *
   * @return The message sender.
   */
  public CarbonPlayer sender() {
    return this.sender;
  }

  /**
   * Get the original message that was sent.
   *
   * @return The original message.
   */
  public Component originalMessage() {
    return this.originalMessage;
  }

  /**
   * Get the chat message that will be sent.
   *
   * @return The chat message.
   */
  public Component message() {
    return this.message;
  }

  /**
   * Set the chat message that will be sent.
   *
   * @param message new message
   */
  public void message(final @NonNull Component message) {
    this.message = message;
  }

  /**
   * The players that will receive the message.
   * List is mutable and players may be added/removed.
   *
   * @return The players that will receive this message.
   */
  public @NonNull List<@NonNull CarbonPlayer> recipients() {
    return this.recipients;
  }

}
