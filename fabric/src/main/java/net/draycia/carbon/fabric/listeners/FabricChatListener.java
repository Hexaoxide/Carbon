package net.draycia.carbon.fabric.listeners;

import java.util.function.Consumer;
import net.draycia.carbon.fabric.callback.FabricChatCallback;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class FabricChatListener implements Consumer<FabricChatCallback.Chat> {

    private static final FabricChatCallback.Chat.MessageFormatter FORMATTER = (sender, message, viewer) -> Component.translatable(
        "chat.type.text",
        FabricServerAudiences.of(sender.server).toAdventure(sender.getDisplayName()),
        MiniMessage.miniMessage().deserialize(message)
    );

    @Override
    public void accept(final FabricChatCallback.Chat chat) {
        chat.formatter(FORMATTER);
    }

}
