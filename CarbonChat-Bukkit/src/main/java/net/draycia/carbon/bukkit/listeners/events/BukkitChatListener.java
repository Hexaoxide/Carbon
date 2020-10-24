package net.draycia.carbon.bukkit.listeners.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.events.UserEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.ConsoleUser;
import net.draycia.carbon.bukkit.CarbonChatBukkit;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

public class BukkitChatListener implements Listener {

  private final @NonNull CarbonChatBukkit carbonChat;

  public BukkitChatListener(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;
  }

  // Chat messages
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerChat(final @NonNull AsyncPlayerChatEvent event) {
    final PlayerUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      channel = this.carbonChat.channelRegistry().defaultValue();
    }

    //    if (channel.shouldCancelChatEvent()) {
    //      event.setCancelled(true);
    //    }

    for (final ChatChannel entry : this.carbonChat.channelRegistry()) {
      if (!(entry instanceof TextChannel)) {
        continue;
      }

      final TextChannel textChannel = (TextChannel) entry;
      final String prefix = textChannel.messagePrefix();

      if (prefix == null || prefix.isEmpty()) {
        continue;
      }

      if (event.getMessage().startsWith(prefix)) {
        if (entry.canPlayerUse(user)) {
          event.setMessage(event.getMessage().substring(prefix.length()));
          channel = entry;
          break;
        }
      }
    }

    final ChatChannel selectedChannel = channel;

    if (!selectedChannel.canPlayerUse(user)) {
      return;
    }

    event.getRecipients().clear();

    if (event.isAsynchronous()) {
      final Map<CarbonUser, Component> messages =
        selectedChannel.parseMessage(user, event.getMessage(), false);

      for (final Map.Entry<CarbonUser, Component> entry : messages.entrySet()) {
        if (entry.getValue().equals(Component.empty())) {
          continue;
        }

        if (user instanceof ConsoleUser) {
          event.setFormat(PlainComponentSerializer.plain().serialize(entry.getValue())
            .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
        } else {
          entry.getKey().sendMessage(user.identity(), entry.getValue());
        }
      }
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        selectedChannel.sendComponentsAndLog(user.identity(),
          selectedChannel.parseMessage(user, event.getMessage(), false));

        //        if (this.carbonChat.getConfig().getBoolean("show-tips")) {
        //          this.carbonChat.logger().info("Tip: Sync chat event! I cannot set the message format due to this. :(");
        //          this.carbonChat.logger().info("Tip: To 'solve' this, do a binary search and see which plugin is triggering");
        //          this.carbonChat.logger().info("Tip: sync chat events and causing this, and let that plugin author know.");
        //        }
      });
    }
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final PlayerUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final UserEvent.Join joinEvent = new UserEvent.Join(user);

    CarbonEvents.post(joinEvent);
  }

  @EventHandler
  public void onPlayerLeave(final PlayerQuitEvent event) {
    final PlayerUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final UserEvent.Leave leaveEvent = new UserEvent.Leave(user);

    CarbonEvents.post(leaveEvent);
  }

}
