package net.draycia.carbon.util;

import dev.jorel.commandapi.CommandAPI;
import net.draycia.carbon.storage.CommandSettings;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandUtils {

  public static void handleDuplicateCommands(@NonNull CommandSettings settings) {
    CommandAPI.unregister(settings.getName());

    for (String command : settings.getAliases()) {
      CommandAPI.unregister(command);
    }
  }
}
