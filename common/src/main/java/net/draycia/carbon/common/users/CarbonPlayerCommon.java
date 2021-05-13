package net.draycia.carbon.common.users;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.text;

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

}
