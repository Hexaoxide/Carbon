package net.draycia.carbon.bukkit.listeners;

import com.google.inject.Inject;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.channels.BasicChatChannel;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

public final class BukkitChatListener implements Listener {

    private final CarbonChat carbonChat;
    private final BasicChatChannel basicChat;

    @Inject
    public BukkitChatListener(final CarbonChat carbonChat, final BasicChatChannel basicChat) {
        this.carbonChat = carbonChat;
        this.basicChat = basicChat;
    }

    @EventHandler
    public void onPlayerChat(final @NonNull AsyncChatEvent event) {
        final var playerResult = this.carbonChat.server().player(event.getPlayer().getUniqueId()).join();

        if (playerResult.player() == null) {
            return;
        }

        final var sender = playerResult.player();
        final var channel = requireNonNullElse(sender.selectedChannel(), this.basicChat);

        // TODO: option to specify if the channel should invoke ChatChannel#recipients
        //   or ChatChannel#filterRecipients
        //   for now we will just always invoke ChatChannel#recipients
        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, event.message(), recipients, renderers);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        try {
            event.viewers().clear();
            event.viewers().addAll(chatEvent.recipients());
        } catch (final UnsupportedOperationException ignored) {
            // Do we log something here? Would get spammy fast.
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            final @NonNull AsyncChatEvent eventRef = event;
            Component component = message;

            for (final var renderer : chatEvent.renderers()) {
                component = renderer.render(sender, viewer, component, message);
            }

            return component;
        });
    }

}
