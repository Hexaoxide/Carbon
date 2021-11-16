package net.draycia.carbon.fabric.listeners;

import java.util.ArrayList;
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
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
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

    private static final FabricChatCallback.Chat.MessageFormatter FORMATTER = (sender, message, viewer) -> Component.translatable(
        "chat.type.text",
        FabricServerAudiences.of(sender.server).toAdventure(sender.getDisplayName()),
        MiniMessage.miniMessage().deserialize(message)
    );

    public FabricChatListener(CarbonChatFabric carbonChatFabric, ChannelRegistry channelRegistry) {
        this.carbonChatFabric = carbonChatFabric;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public void accept(final FabricChatCallback.Chat chat) {
        chat.formatter(FORMATTER);

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

            for (final var recipient : chatEvent.recipients()) {
                var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

                for (final var renderer : chatEvent.renderers()) {
                    try {
                        if (recipient instanceof ServerPlayer player) {
                            final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChatFabric.server().player(player.getUUID()).join();

                            renderedMessage = renderer.render(sender, targetPlayer.player(), renderedMessage.component(), chatEvent.message());
                        } else {
                            renderedMessage = renderer.render(sender, recipient, renderedMessage.component(), chatEvent.message());
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }

                final Identity identity;

                if (sender.hasPermission("carbon.hideidentity")) {
                    identity = Identity.nil();
                } else {
                    identity = sender.identity();
                }

                recipient.sendMessage(identity, renderedMessage.component(), renderedMessage.messageType());
            }
        }
    }

}
