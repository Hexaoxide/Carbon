package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlayerJoinListener implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  public PlayerJoinListener(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler
  public void onPlayerJoin(final PlayerJoinEvent event) {
    final ChatUser user = this.carbonChat.getUserService().wrap(event.getPlayer());

    this.carbonChat.getUserService().validate(user);

    if (user.nickname() != null) {
      user.nickname(user.nickname());
    }

    final String channel = this.carbonChat.getConfig().getString("channel-on-join");

    if (channel == null || channel.isEmpty()) {
      return;
    }

    if (channel.equals("DEFAULT")) {
      user.selectedChannel(this.carbonChat.getChannelManager().defaultChannel());
      return;
    }

    final ChatChannel chatChannel = this.carbonChat.getChannelManager().registry().channel(channel);

    if (chatChannel != null) {
      user.selectedChannel(chatChannel);
    }
  }

  @EventHandler
  public void onPlayerQuit(final PlayerQuitEvent event) {
    final ChatUser user = this.carbonChat.getUserService().wrapIfLoaded(event.getPlayer());

    if (user != null) {
      this.carbonChat.getUserService().invalidate(user);
    }
  }

}
