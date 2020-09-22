package net.draycia.carbon.listeners.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.CarbonChatBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.HashSet;

public class BukkitChatListener implements Listener {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public BukkitChatListener(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;
  }

  // Chat messages
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerchat(final @NonNull AsyncPlayerChatEvent event) {
    final ChatUser user = this.carbonChat.userService().wrap(event.getPlayer().getUniqueId());
    ChatChannel channel = user.selectedChannel();

    if (channel == null) {
      if (this.carbonChat.channelRegistry().defaultChannel() == null) {
        return;
      }

      channel = this.carbonChat.channelRegistry().defaultChannel();
    }

    if (channel.shouldCancelChatEvent()) {
      event.setCancelled(true);
    }

    for (final ChatChannel entry : this.carbonChat.channelRegistry()) {
      if (entry.messagePrefix() == null || entry.messagePrefix().isEmpty()) {
        continue;
      }

      if (event.getMessage().startsWith(entry.messagePrefix())) {
        if (entry.canPlayerUse(user)) {
          event.setMessage(event.getMessage().substring(entry.messagePrefix().length()));
          channel = entry;
          break;
        }
      }
    }

    final ChatChannel selectedChannel = channel;

    if (!selectedChannel.canPlayerUse(user)) {
      return;
    }

    final Collection<ChatUser> recipients;

    if (selectedChannel.honorsRecipientList()) {
      recipients = new HashSet<>();

      for (final Player recipient : event.getRecipients()) {
        recipients.add(this.carbonChat.userService().wrap(recipient.getUniqueId()));
      }
    } else {
      recipients = selectedChannel.audiences();
    }

    event.getRecipients().clear();

    if (event.isAsynchronous()) {
      final Component component = selectedChannel.sendMessage(user, recipients, event.getMessage(), false);

      if (!component.equals(TextComponent.empty())) {
        event.setFormat(PlainComponentSerializer.plain().serialize(component)
          .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
      }
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Component component = selectedChannel.sendMessage(user, recipients, event.getMessage(), false);

        this.carbonChat.messageProcessor().audiences().console().sendMessage(component);

        if (this.carbonChat.getConfig().getBoolean("show-tips")) {
          this.carbonChat.logger().info("Tip: Sync chat event! I cannot set the message format due to this. :(");
          this.carbonChat.logger().info("Tip: To 'solve' this, do a binary search and see which plugin is triggering");
          this.carbonChat.logger().info("Tip: sync chat events and causing this, and let that plugin author know.");
        }
      });
    }
  }

}
