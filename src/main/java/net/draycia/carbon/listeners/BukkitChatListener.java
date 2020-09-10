package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
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
  private final CarbonChat carbonChat;

  public BukkitChatListener(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  // Chat messages
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerchat(@NonNull final AsyncPlayerChatEvent event) {
    final ChatUser user = this.carbonChat.getUserService().wrap(event.getPlayer());
    ChatChannel channel = user.selectedChannel();

    if (channel.shouldCancelChatEvent()) {
      event.setCancelled(true);
    }

    for (final ChatChannel entry : this.carbonChat.getChannelManager().registry().values()) {
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
        recipients.add(this.carbonChat.getUserService().wrap(recipient));
      }
    } else {
      recipients = selectedChannel.audiences();
    }

    event.getRecipients().clear();

    if (event.isAsynchronous()) {
      final Component component = selectedChannel.sendMessage(user, recipients, event.getMessage(), false);

      event.setFormat(CarbonChat.LEGACY.serialize(component)
        .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Component component = selectedChannel.sendMessage(user, recipients, event.getMessage(), false);

        this.carbonChat.getAdventureManager().audiences().console().sendMessage(component);

        if (this.carbonChat.getConfig().getBoolean("show-tips")) {
          this.carbonChat.getLogger().info("Tip: Sync chat event! I cannot set the message format due to this. :(");
          this.carbonChat.getLogger().info("Tip: To 'solve' this, do a binary search and see which plugin is triggering");
          this.carbonChat.getLogger().info("Tip: sync chat events and causing this, and let that plugin author know.");
        }
      });
    }
  }

}
