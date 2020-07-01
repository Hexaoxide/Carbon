package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {

    private SimpleChat simpleChat;

    public PlayerListener(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        if (event.isAsynchronous()) {
            simpleChat.getPlayerChannel(event.getPlayer()).sendMessage(event.getPlayer(), event.getMessage());
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                simpleChat.getPlayerChannel(event.getPlayer()).sendMessage(event.getPlayer(), event.getMessage());
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // bug hunting
        String format = "[Test] Test [format] btw.";
        TextComponent component = TextComponent.of(format).color(TextColor.fromHexString("#00FF00"));

        HoverEvent hoverEvent = HoverEvent.showText(TextComponent.of("kittens"));

        component = component.replace(Pattern.compile(Pattern.quote("[format]")), (builder) -> {
           return TextComponent.builder().append("[message]", TextColor.fromHexString("#FF00FF")).hoverEvent(hoverEvent);
        });

        simpleChat.getAudiences().audience(event.getPlayer()).sendMessage(component);
    }

}
