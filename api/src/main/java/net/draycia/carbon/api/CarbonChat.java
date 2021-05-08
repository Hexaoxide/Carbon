package net.draycia.carbon.api;

import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface CarbonChat {

  @NonNull Logger logger();

  @NonNull Component createComponent(final @NonNull UUID uuid);

}
