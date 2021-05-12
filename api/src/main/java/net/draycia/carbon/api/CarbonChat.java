package net.draycia.carbon.api;

import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CarbonChat {

  @NonNull Logger logger();

  @NonNull CarbonEventHandler eventHandler();

  @NonNull UserManager userManager();

}
