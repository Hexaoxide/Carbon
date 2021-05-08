package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

@Plugin("carbonchat")
public class CarbonChatSponge implements CarbonChat {
  private final PluginContainer pluginContainer;
  private final Logger logger;

  private static final int BSTATS_PLUGIN_ID = 11279;

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

  @Override
  public @NonNull Component createComponent(final @NonNull UUID uuid) {
    final Optional<ServerPlayer> player = Sponge.server().player(uuid);

    if (player.isPresent()) {
      ItemStack itemStack = null;

      final ItemStack mainHand = player.get().itemInHand(HandTypes.MAIN_HAND);

      if (!mainHand.isEmpty()) {
        itemStack = mainHand;
      } else {
        final ItemStack offHand = player.get().itemInHand(HandTypes.OFF_HAND);

        if (!offHand.isEmpty()) {
          itemStack = offHand;
        }
      }

      if (itemStack == null || itemStack.isEmpty()) {
        return empty();
      }

      final TextComponent.Builder builder = text();

      builder.append(text("[", WHITE));
      builder.append(itemStack.get(Keys.DISPLAY_NAME).orElse(itemStack.type().asComponent()));
      builder.append(text("]", WHITE));

      return builder.build();
    }

    return empty();
  }
}
