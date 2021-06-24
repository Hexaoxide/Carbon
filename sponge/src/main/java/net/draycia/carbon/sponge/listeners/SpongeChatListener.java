package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

public final class SpongeChatListener {

    private final CarbonChatSponge carbonChat;
    private final ChannelRegistry registry;

    @Inject
    private SpongeChatListener(
        final CarbonChat carbonChat,
        final ChannelRegistry registry
    ) {
        this.carbonChat = (CarbonChatSponge) carbonChat;
        this.registry = registry;
    }

    @Listener
    public void onPlayerChat(final @NonNull PlayerChatEvent event, final @First Player source) {
        final var result = this.carbonChat.server().player(source.uniqueId()).join();

        if (result.player() == null) {
            this.carbonChat.server().console().sendMessage(result.reason());
            return;
        }

        final var channel = requireNonNullElse(result.player().selectedChannel(),
            this.registry.defaultValue());

        // TODO: option to specify if the channel should invoke ChatChannel#recipients
        //   or ChatChannel#filterRecipients
        //   for now we will just always invoke ChatChannel#recipients
        final var recipients = channel.recipients(result.player());

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(result.player(), event.message(), recipients, renderers);
        final var eventResult = this.carbonChat.eventHandler().emit(chatEvent);

        if (!eventResult.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                result.player().sendMessage(message);
            }

            return;
        }

        try {
            event.setAudience(Audience.audience(chatEvent.recipients()));
        } catch (final UnsupportedOperationException exception) {
            exception.printStackTrace();
            // Do we log something here? Would get spammy fast.
        }

        event.setChatFormatter((player, target, message, originalMessage) -> {
            Component component = message;

            for (final var renderer : chatEvent.renderers()) {
                if (target instanceof ServerPlayer serverPlayer) {
                    final ComponentPlayerResult targetPlayer = this.carbonChat.server().player(serverPlayer).join();
                    component = renderer.render(result.player(), targetPlayer.player(), component, message);
                } else {
                    component = renderer.render(result.player(), target, component, message);
                }
            }

            if (component == Component.empty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(component);
        });
    }

}
