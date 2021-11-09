package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

@DefaultQualifier(NonNull.class)
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
    public void onPlayerChat(final PlayerChatEvent event, final @First Player source) {
        final var playerResult = this.carbonChat.server().player(source.uniqueId()).join();

        if (playerResult.player() == null) {
            this.carbonChat.server().console().sendMessage(playerResult.reason());
            return;
        }

        final var channel = requireNonNullElse(playerResult.player().selectedChannel(),
            this.registry.defaultValue());

        // TODO: option to specify if the channel should invoke ChatChannel#recipients
        //   or ChatChannel#filterRecipients
        //   for now we will just always invoke ChatChannel#recipients
        final var recipients = channel.recipients(playerResult.player());

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(playerResult.player(), event.message(), recipients, renderers, channel);
        final var eventResult = this.carbonChat.eventHandler().emit(chatEvent);

        if (!eventResult.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                playerResult.player().sendMessage(message);
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
                    final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChat.server().player(serverPlayer).join();
                    component = renderer.render(playerResult.player(), targetPlayer.player(), component, message).component();
                } else {
                    component = renderer.render(playerResult.player(), target, component, message).component();
                }
            }

            if (component == Component.empty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(component);
        });
    }

}
