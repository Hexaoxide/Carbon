package net.draycia.carbon.util;

import dev.jorel.commandapi.CommandAPI;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandUtils {

  private CommandUtils() {

  }

  public static void handleDuplicateCommands(final @NonNull CommandSettings settings) {
    CommandAPI.unregister(settings.name());

    for (final String command : settings.aliases()) {
      CommandAPI.unregister(command);
    }
  }

}
