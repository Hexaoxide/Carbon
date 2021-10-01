package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;

@DefaultQualifier(NonNull.class)
public class MuteHandler {

    private final Key muteKey = key("carbon", "mute");
    private CarbonMessageService messageService;

    private final KeyedRenderer renderer =
        keyedRenderer(this.muteKey, (sender, recipient, message, originalMessage) ->
            this.messageService.muteSpyPrefix(recipient).append(message));

    @Inject
    public MuteHandler(
        final CarbonChat carbonChat,
        final CarbonMessageService messageService
    ) {
        this.messageService = messageService;

        carbonChat.eventHandler().subscribe(CarbonChatEvent.class, 100, false, event -> {
            if (!event.sender().muted(event.chatChannel())) {
                return;
            }

            event.renderers().add(this.renderer);

            // TODO: ShadowMuteHandler? Include that logic in here?
            event.recipients().removeIf(entry -> entry instanceof CarbonPlayer carbonPlayer &&
                !carbonPlayer.spying());
        });
    }

}
