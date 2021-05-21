package net.draycia.carbon.bukkit.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.CarbonChatBukkit;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Map;

import static net.draycia.carbon.common.Injector.byInject;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

public final class BukkitChatListener implements Listener {

    private final CarbonChatBukkit carbonChat = byInject(CarbonChat.class); // lol this is dumb
    private final UserManager userManager = this.carbonChat.userManager();

    @EventHandler
    public void onPlayerChat(final @NonNull AsyncChatEvent event) {
        final var sender = this.userManager.carbonPlayer(event.getPlayer().getUniqueId());

        if (sender == null) {
            return;
        }

        final var recipients = new ArrayList<Audience>();
        final var channel = sender.selectedChannel();

        for (final Player bukkitRecipient : event.recipients()) {
            final var recipient = this.userManager.carbonPlayer(bukkitRecipient.getUniqueId());

            // The naming is a little backwards, ChatChannel#mayReceiveMessages
            // TODO: Change it at some point
            if (recipient != null && channel.mayReceiveMessages(recipient)) {
                recipients.add(recipient);
            }
        }

        // console too!
        recipients.add(Bukkit.getConsoleSender());

        final var chatEvent = new CarbonChatEvent(sender, event.message(), recipients,
            Map.of(key("carbon", "default"), channel.renderer()));
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (result.wasSuccessful()) {
            for (final var recipient : chatEvent.recipients()) {
                var component = chatEvent.message();

                for (final var entry : chatEvent.renderers().entrySet()) {
                    component = entry.getValue().render(sender,
                        recipient, chatEvent.message(), chatEvent.originalMessage());
                }

                if (component != null) {
                    recipient.sendMessage(component);
                }
            }
        } else if (chatEvent.result().cancelled()) {

            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }
        }

        // There's no guarantee that recipients is mutable.
        // If it's ever immutable, we yell at who ever is responsible.
        event.recipients().clear();
    }

}
