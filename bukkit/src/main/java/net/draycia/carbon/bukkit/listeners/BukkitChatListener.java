package net.draycia.carbon.bukkit.listeners;

import com.google.inject.Inject;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;

public final class BukkitChatListener implements Listener {

    @Inject
    private CarbonChat carbonChat;

    @EventHandler
    public void onPlayerChat(final @NonNull AsyncChatEvent event) {
        final var sender = this.carbonChat.server().player(event.getPlayer().getUniqueId());

        if (sender == null) {
            return;
        }

        final var recipients = new ArrayList<Audience>();
        final var channel = sender.selectedChannel();

        for (final Player bukkitRecipient : event.recipients()) {
            final var recipient = this.carbonChat.server().player(bukkitRecipient.getUniqueId());

            if (recipient != null && channel.hearingPermitted(recipient).permitted()) {
                recipients.add(recipient);
            }
        }

        // console too!
        recipients.add(Bukkit.getConsoleSender());

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel.renderer()));

        final var chatEvent = new CarbonChatEvent(sender, event.message(), recipients, renderers);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (result.wasSuccessful()) {
            // TODO: send to channels

            for (final var recipient : chatEvent.recipients()) {
                var component = chatEvent.message();

                for (final var renderer : chatEvent.renderers()) {
                    component = renderer.render(sender,
                        recipient, chatEvent.message(), chatEvent.originalMessage());
                }

                if (component != null) {
                    recipient.sendMessage(component);
                }
            }
        }

        // There's no guarantee that recipients is mutable.
        // If it's ever immutable, we yell at who ever is responsible.
        event.recipients().clear();
    }

}
