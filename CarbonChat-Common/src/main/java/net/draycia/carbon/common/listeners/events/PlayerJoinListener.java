package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.UserEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.users.CarbonUser;

public class PlayerJoinListener {

  public PlayerJoinListener() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(UserEvent.Join.class, event -> {
      final CarbonUser user = event.user();

      carbonChat.userService().validate(user);

      if (user.nickname() != null) {
        user.nickname(user.nickname());
      }

      final String channel = carbonChat.carbonSettings().channelOnJoin();

      if (channel == null || channel.isEmpty()) {
        return;
      }

      if (channel.equals("DEFAULT")) {
        user.selectedChannel(carbonChat.channelRegistry().defaultChannel());
        return;
      }

      final ChatChannel chatChannel = carbonChat.channelRegistry().get(channel);

      if (chatChannel != null) {
        user.selectedChannel(chatChannel);
      }
    });

    CarbonEvents.register(UserEvent.Leave.class, event -> {
      carbonChat.userService().invalidate(event.user());
    });
  }

}
