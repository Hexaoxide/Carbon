package net.draycia.carbon.bukkit.listeners;

import com.google.inject.Inject;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.bukkit.CarbonChatBukkit;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

@DefaultQualifier(NonNull.class)
public final class BukkitChatListener implements Listener {

    private final CarbonChatBukkit carbonChat;
    private final ChannelRegistry registry;

    @Inject
    public BukkitChatListener(final CarbonChat carbonChat, final ChannelRegistry registry) {
        this.carbonChat = (CarbonChatBukkit) carbonChat;
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(final @NonNull AsyncChatEvent event) {
        final var playerResult = this.carbonChat.server().player(event.getPlayer().getUniqueId()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(),
            this.registry.defaultValue());

        final var originalMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());

        for (final var chatChannel : this.registry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (originalMessage.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                break;
            }
        }

        // TODO: option to specify if the channel should invoke ChatChannel#recipients
        //   or ChatChannel#filterRecipients
        //   for now we will just always invoke ChatChannel#recipients
        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, event.message(), recipients, renderers, channel);
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
        } catch (final UnsupportedOperationException exception) {
            exception.printStackTrace();
        }

        if (sender.hasPermission("carbon.hideidentity")) {
            for (final var recipient : chatEvent.recipients()) {
                var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

                for (final var renderer : chatEvent.renderers()) {
                    try {
                        if (recipient instanceof Player player) {
                            final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChat.server().player(player).join();

                            renderedMessage = renderer.render(sender, targetPlayer.player(), renderedMessage.component(), chatEvent.message());
                        } else {
                            renderedMessage = renderer.render(sender, recipient, renderedMessage.component(), chatEvent.message());
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }

                recipient.sendMessage(Identity.nil(), renderedMessage.component(), renderedMessage.messageType());
            }
        } else {
            try {
                event.viewers().addAll(chatEvent.recipients());
            } catch (final UnsupportedOperationException exception) {
                exception.printStackTrace();
            }

            event.renderer((source, sourceDisplayName, message, viewer) -> {
                var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

                for (final var renderer : chatEvent.renderers()) {
                    try {
                        if (viewer instanceof Player player) {
                            final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChat.server().player(player).join();
                            renderedMessage = renderer.render(sender, targetPlayer.player(), renderedMessage.component(), message);
                        } else {
                            renderedMessage = renderer.render(sender, viewer, renderedMessage.component(), message);
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }

                return renderedMessage.component();
            });
        }
    }

}
