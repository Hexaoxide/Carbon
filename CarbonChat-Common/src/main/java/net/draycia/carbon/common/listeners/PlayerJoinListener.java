package net.draycia.carbon.common.listeners;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.UserEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.text.Component;

public class PlayerJoinListener {

  public PlayerJoinListener() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(UserEvent.Join.class, event -> {
      final PlayerUser user = event.user();

      carbonChat.userService().validate(user);

      final Component nickname = user.nickname();

      if (nickname != null) {
        user.nickname(nickname);
      }

      final String channel = carbonChat.carbonSettings().channelOnJoin();

      if (channel == null || channel.isEmpty()) {
        return;
      }

      if (channel.equals("DEFAULT")) {
        user.selectedChannel(carbonChat.channelRegistry().defaultValue());
        return;
      }

      final ChatChannel chatChannel = carbonChat.channelRegistry().get(channel);

      if (chatChannel != null) {
        user.selectedChannel(chatChannel);
      }
    });

    CarbonEvents.register(UserEvent.Leave.class, event -> carbonChat.userService().invalidate(event.user()));
  }

}
