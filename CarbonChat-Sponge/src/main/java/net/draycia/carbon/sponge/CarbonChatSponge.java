package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import org.slf4j.Logger;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
  id = "carbonchat-sponge",
  name = "carbonchat"
)
public class CarbonChatSponge implements CarbonChat {

  @Inject
  private Logger logger;

  @Listener
  public void onServerStart(GameStartedServerEvent event) {
  }

}
