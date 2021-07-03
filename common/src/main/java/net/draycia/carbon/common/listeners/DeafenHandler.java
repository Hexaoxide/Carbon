package net.draycia.carbon.common.listeners;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class DeafenHandler {

    @Inject
    public DeafenHandler(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager
    ) {
        carbonChat.eventHandler().subscribe(CarbonChatEvent.class, 0, false, event -> {
            if (!event.sender().deafened()) {
                return;
            }

            event.recipients().removeIf(entry -> entry instanceof CarbonPlayer carbonPlayer &&
                carbonPlayer.deafened());
        });
    }

}
