package net.draycia.carbon.listeners.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.events.UserEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.CarbonChatBukkit;
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

  private @NonNull final CarbonChatBukkit carbonChat;

  public BukkitChatListener(@NonNull final CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;
  }

  // Chat messages
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerchat(@NonNull final AsyncPlayerChatEvent event) {
    // TODO: Move most of this to Common handling
    // TODO: ChatMessageEvent that's called at the start of this
    // TODO: Obtain a component (for console usage) from that event, or another
    final CarbonUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      if (this.carbonChat.channelRegistry().defaultChannel() == null) {
        return;
      }

      channel = this.carbonChat.channelRegistry().defaultChannel();
    }

    //    if (channel.shouldCancelChatEvent()) {
    //      event.setCancelled(true);
    //    }

    for (final ChatChannel entry : this.carbonChat.channelRegistry()) {
      if (!(entry instanceof TextChannel)) {
        continue;
      }

      final TextChannel textChannel = (TextChannel) entry;

      if (textChannel.messagePrefix() == null || textChannel.messagePrefix().isEmpty()) {
        continue;
      }

      if (event.getMessage().startsWith(textChannel.messagePrefix())) {
        if (entry.canPlayerUse(user)) {
          event.setMessage(event.getMessage().substring(textChannel.messagePrefix().length()));
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

        entry.getKey().sendMessage(entry.getValue());

        if (user.equals(entry.getKey())) {
          event.setFormat(PlainComponentSerializer.plain().serialize(entry.getValue())
            .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
        }
      }
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        selectedChannel.sendComponentsAndLog(
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
    final CarbonUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final UserEvent.Join joinEvent = new UserEvent.Join(user);

    CarbonEvents.post(joinEvent);
  }

  @EventHandler
  public void onPlayerLeave(final PlayerQuitEvent event) {
    final CarbonUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    final UserEvent.Leave leaveEvent = new UserEvent.Leave(user);

    CarbonEvents.post(leaveEvent);
  }

}
