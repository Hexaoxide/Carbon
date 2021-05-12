package net.draycia.carbon.common.command;

import net.draycia.carbon.api.users.CarbonPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PlayerCommander extends Commander {
  @NonNull CarbonPlayer carbonPlayer();
}
