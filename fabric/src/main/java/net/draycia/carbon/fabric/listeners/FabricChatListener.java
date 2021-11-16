package net.draycia.carbon.fabric.listeners;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.draycia.carbon.fabric.callback.FabricChatCallback;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

@DefaultQualifier(NonNull.class)
public class FabricChatListener implements Consumer<FabricChatCallback.Chat> {

    private final CarbonChatFabric carbonChatFabric;
    private final ChannelRegistry channelRegistry;

    public FabricChatListener(CarbonChatFabric carbonChatFabric, ChannelRegistry channelRegistry) {
        this.carbonChatFabric = carbonChatFabric;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public void accept(final FabricChatCallback.Chat chat) {
        final var playerResult = this.carbonChatFabric.server().player(chat.sender().getUUID()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.channelRegistry.defaultValue());
        final var originalMessage = chat.message();

        for (final var chatChannel : this.channelRegistry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (originalMessage.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                break;
            }
        }

        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, Component.text(chat.message()), recipients, renderers, channel);
        final var result = this.carbonChatFabric.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        if (sender.hasPermission("carbon.hideidentity")) {
            chat.identity(Identity.nil());
        }

        chat.formatter(((sender1, message, viewer) -> {
            var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

            for (final var renderer : chatEvent.renderers()) {
                try {
                    final Optional<UUID> uuid = viewer.get(Identity.UUID);
                    if (uuid.isPresent()) {
                        final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChatFabric.server().player(uuid.get()).join();

                        renderedMessage = renderer.render(sender, targetPlayer.player(), renderedMessage.component(), chatEvent.message());
                    } else {
                        renderedMessage = renderer.render(sender, viewer, renderedMessage.component(), chatEvent.message());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            return renderedMessage.component();
        }));
    }

}
