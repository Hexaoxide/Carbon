package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.util.InventorySlots;
import net.kyori.adventure.text.TextReplacementConfig;

public class ItemLinkHandler {

    @Inject
    public ItemLinkHandler(
        final CarbonChat carbonChat
    ) {
        carbonChat.eventHandler().subscribe(CarbonChatEvent.class, 1, true, event -> {
            carbonChat.logger().info("test!");
            for (final var slot : InventorySlots.VALUES) {
                for (final var placeholder : slot.placeholders()) {
                    event.message(
                        event.message()
                            .replaceText(TextReplacementConfig.builder()
                                .matchLiteral("<" + placeholder + ">")
                                .once()
                                .replacement(builder -> {
                                    final var itemComponent = event.sender().createItemHoverComponent(slot);

                                    if (itemComponent == null) {
                                        return builder;
                                    }

                                    return event.sender().createItemHoverComponent(slot);
                                })
                                .build())
                    );
                }
            }
        });
    }

}
