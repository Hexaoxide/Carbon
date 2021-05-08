package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("carbonchat")
public class CarbonChatSponge implements CarbonChat {
  private final PluginContainer pluginContainer;
  private final Logger logger;

  @Inject
  public CarbonChatSponge(
    final @NonNull PluginContainer pluginContainer,
    final @NonNull Logger logger
  ) {
    this.pluginContainer = pluginContainer;
    this.logger = logger;
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }
}
