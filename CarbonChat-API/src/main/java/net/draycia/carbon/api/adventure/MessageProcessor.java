package net.draycia.carbon.api.adventure;

import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MessageProcessor {

  @NonNull Component processMessage(final @Nullable String input, final @NonNull String @NonNull ... placeholders);

  @NonNull AudienceProvider audiences();

  @NonNull FormatType formatType();

}
