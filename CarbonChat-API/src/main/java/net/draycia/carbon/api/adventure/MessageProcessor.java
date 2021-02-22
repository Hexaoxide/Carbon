package net.draycia.carbon.api.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MessageProcessor {

  @NonNull Component processMessage(final @Nullable String input, final @NonNull Template @NonNull ... templates);

  @NonNull FormatType formatType();

}
