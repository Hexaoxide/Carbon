package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.users.CarbonUser;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;

public interface GroupChannel extends ChatChannel, ForwardingAudience {

  @NonNull CarbonUser owner();

  @Override
  @NonNull Collection<@NonNull CarbonUser> audiences();

}
