package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("carbonchat")
public class CarbonChatSponge implements CarbonChat {
  private final PluginContainer pluginContainer;
  private final Logger logger;

  private static final int BSTATS_PLUGIN_ID = 8720;

  @Inject
  public CarbonChatSponge(
    final @NonNull PluginContainer pluginContainer,
    final @NonNull Logger logger,
    final Metrics.Factory metricsFactory
  ) {
    this.pluginContainer = pluginContainer;
    this.logger = logger;

    metricsFactory.make(BSTATS_PLUGIN_ID);
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }
}
