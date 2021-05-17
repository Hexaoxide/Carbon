package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.ChatComponentRenderer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@DefaultQualifier(NonNull.class)
public class VanillaChatChannel implements ChatChannel {

    private final @NonNull ChatComponentRenderer renderer =
        (sender, recipient, message, originalMessage) -> {
            final var clickEvent = ClickEvent.suggestCommand("/msg " + sender.username());
            final var hoverEvent = HoverEvent.showEntity(Key.key("player"),
                sender.uuid(), text(sender.username()));

            final var name = sender.displayName()
                .clickEvent(clickEvent)
                .hoverEvent(hoverEvent)
                .insertion(sender.username());

            return translatable("chat.type.text", name.color(TextColor.color(0xff0000)), message);
        };

    @Override
    public boolean mayReceiveMessages(final CarbonPlayer player) {
        return true;
    }

    @Override
    public boolean maySendMessages(final CarbonPlayer player) {
        return true;
    }

    @Override
    public ChatComponentRenderer renderer() {
        return this.renderer;
    }
}
