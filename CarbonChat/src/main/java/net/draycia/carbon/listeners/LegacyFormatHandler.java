package net.draycia.carbon.listeners;

import net.draycia.carbon.events.ChatFormatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LegacyFormatHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyFormat(ChatFormatEvent event) {
        Component component = LegacyComponentSerializer.legacy('&').deserialize(event.getFormat());
        event.setFormat(MiniMessage.get().serialize(component));
    }

}
