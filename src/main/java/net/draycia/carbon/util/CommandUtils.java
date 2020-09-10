package net.draycia.carbon.util;

import dev.jorel.commandapi.CommandAPI;
import net.draycia.carbon.storage.CommandSettings;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandUtils {

  private CommandUtils() {

  }

  public static void handleDuplicateCommands(@NonNull final CommandSettings settings) {
    CommandAPI.unregister(settings.name());

    for (final String command : settings.aliases()) {
      CommandAPI.unregister(command);
    }
  }

}
