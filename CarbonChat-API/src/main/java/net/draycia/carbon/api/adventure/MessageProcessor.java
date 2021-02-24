package net.draycia.carbon.api.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MessageProcessor {

  /**
   * Applies the templates to the supplied input and parses it into a component
   * @param input The message format
   * @param templates Placeholders / replacements
   * @return The finished component
   */
  @NonNull Component processMessage(final @Nullable String input, final @NonNull Template @NonNull ... templates);

  /**
   * The parser used to create components
   * @return The parser used to create components
   */
  @NonNull FormatType formatType();

}
