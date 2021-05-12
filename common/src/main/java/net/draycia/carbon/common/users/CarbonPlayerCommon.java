package net.draycia.carbon.common.users;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public abstract class CarbonPlayerCommon implements CarbonPlayer, ForwardingAudience.Single {

  protected final @NonNull String username;
  protected @NonNull Component displayName;
  protected final @NonNull UUID uuid;
  protected final @NonNull Identity identity;

  protected CarbonPlayerCommon(
    final @NonNull String username,
    final @NonNull Component displayName,
    final @NonNull UUID uuid,
    final @NonNull Identity identity
  ) {
    this.username = username;
    this.displayName = displayName;
    this.uuid = uuid;
    this.identity = identity;
  }

  @Override
  public @NonNull String username() {
    return this.username;
  }

  @Override
  public @NonNull Component displayName() {
    return this.displayName;
  }

  @Override
  public void displayName(final @Nullable Component displayName) {
    this.displayName = requireNonNullElseGet(displayName, () -> text(this.username));
  }

  @Override
  public @NonNull Identity identity() {
    return this.identity;
  }

  @Override
  public @NonNull UUID uuid() {
    return this.uuid;
  }

  protected final @NonNull Component createItemHoverComponent(
    final @NonNull Component displayName,
    final @NonNull HoverEventSource<HoverEvent.ShowItem> itemStack
  ) {
    final TextComponent.Builder builder = text(); // Empty root - prevents style leaking.

    builder.hoverEvent(itemStack); // Let this be inherited by all coming components.

    builder.append(text("[", WHITE));
    builder.append(displayName);
    builder.append(text("]", WHITE));

    return builder.build();
  }

}
