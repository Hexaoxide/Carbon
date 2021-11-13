package net.draycia.carbon.fabric;

import net.draycia.carbon.fabric.callback.FabricChatCallback;
import net.fabricmc.api.ModInitializer;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.translatable;

@DefaultQualifier(NonNull.class)
public final class CarbonFabricEntry implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricChatCallback.setup();

        final FabricChatCallback.Chat.MessageFormatter formatter = (sender, message, viewer) -> translatable(
            "chat.type.text",
            FabricServerAudiences.of(sender.server).toAdventure(sender.getDisplayName()),
            MiniMessage.miniMessage().deserialize(message)
        );

        FabricChatCallback.INSTANCE.registerListener(chat -> chat.formatter(formatter));
    }

}
