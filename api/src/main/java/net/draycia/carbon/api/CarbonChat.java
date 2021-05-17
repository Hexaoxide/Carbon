package net.draycia.carbon.api;

import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * The main plugin interface.<br>
 * Instances may be obtained through {@link CarbonChatProvider#carbonChat()}.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonChat {

    /**
     * The plugin's logger.<br>
     * All messages will be logged through this.
     *
     * @return the plugin's logger
     * @since 2.0.0
     */
    Logger logger();

    /**
     * The event handler, used for listening to and emitting events.
     *
     * @return the event handler
     * @since 2.0.0
     */
    CarbonEventHandler eventHandler();

    /**
     * The user manager, used for loading and obtaining {@link CarbonPlayer CarbonPlayers}.
     *
     * @return the user manager
     * @since 2.0.0
     */
    UserManager userManager();

}
