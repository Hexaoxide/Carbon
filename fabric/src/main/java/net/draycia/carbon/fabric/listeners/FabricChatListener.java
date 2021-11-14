package net.draycia.carbon.fabric.listeners;

import java.util.function.Consumer;
import net.draycia.carbon.fabric.callback.FabricChatCallback;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class FabricChatListener implements Consumer<FabricChatCallback.Chat> {

    @Override
    public void accept(FabricChatCallback.Chat chat) {
        final FabricChatCallback.Chat.MessageFormatter formatter = (sender, message, viewer) -> Component.translatable(
            "chat.type.text",
            FabricServerAudiences.of(sender.server).toAdventure(sender.getDisplayName()),
            MiniMessage.miniMessage().deserialize(message)
        );
    }

}
