package net.draycia.carbon.api.events;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.ChatComponentRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;

/**
 * {@link CancellableCarbonEvent} that's called when chat components are rendered for online players.
 *
 * @since 2.0.0
 */
public class CarbonChatEvent extends CancellableCarbonEvent {

  private final @NonNull Map<Key, ChatComponentRenderer> renderers;

  private final @NonNull CarbonPlayer sender;

  private final @NonNull Component originalMessage;

  private @NonNull Component message;

  private final @NonNull List<@NonNull ? extends Audience> recipients;

  /**
   * {@link CancellableCarbonEvent} that's called when players send messages in chat.
   *
   * @param sender the sender of the message
   * @param originalMessage the original message that was sent
   * @param recipients the recipients of the message
   * @param renderers the renderers of the message
   *
   * @since 2.0.0
   */
  public CarbonChatEvent(
    final @NonNull CarbonPlayer sender,
    final @NonNull Component originalMessage,
    final @NonNull List<@NonNull ? extends Audience> recipients,
    final @NonNull Map<Key, ChatComponentRenderer> renderers
  ) {
    this.sender = sender;
    this.originalMessage = originalMessage;
    this.message = originalMessage;
    this.recipients = recipients;
    this.renderers = renderers;
  }

  /**
   * Get the renderers used to construct components for each of the recipients.
   *
   * @return The per-recipient component renderers.
   *
   * @since 2.0.0
   */
  public @NonNull Map<Key, ChatComponentRenderer> renderers() {
    return this.renderers;
  }

  /**
   * Get the sender of the message.
   *
   * @return The message sender.
   *
   * @since 2.0.0
   */
  public CarbonPlayer sender() {
    return this.sender;
  }

  /**
   * Get the original message that was sent.
   *
   * @return The original message.
   *
   * @since 2.0.0
   */
  public Component originalMessage() {
    return this.originalMessage;
  }

  /**
   * Get the chat message that will be sent.
   *
   * @return The chat message.
   *
   * @since 2.0.0
   */
  public Component message() {
    return this.message;
  }

  /**
   * Set the chat message that will be sent.
   *
   * @param message new message
   *
   * @since 2.0.0
   */
  public void message(final @NonNull Component message) {
    this.message = message;
  }

  /**
   * The recipients of the message.
   * List is mutable and elements may be added/removed.
   *
   * @return the recipients of the message.
   *     entries may be players, console, or other audience implementations
   *
   * @since 2.0.0
   */
  public @NonNull List<@NonNull ? extends Audience> recipients() {
    return this.recipients;
  }

}
