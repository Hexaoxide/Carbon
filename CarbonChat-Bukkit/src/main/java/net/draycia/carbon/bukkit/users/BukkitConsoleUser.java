package net.draycia.carbon.bukkit.users;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ConsoleUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

public class BukkitConsoleUser implements ConsoleUser, ForwardingAudience {

  private final @NonNull CarbonChat carbonChat;
  private final @NonNull Iterable<Audience> audience;

  public BukkitConsoleUser(final @NonNull ConsoleCommandSender sender) {
    final Plugin plugin = Bukkit.getPluginManager().getPlugin("CarbonChat-Bukkit");

    this.carbonChat = CarbonChatProvider.carbonChat();
    this.audience = Collections.singletonList(BukkitAudiences.create(plugin).sender(sender));
  }

  @Override
  public @NonNull Iterable<? extends Audience> audiences() {
    return this.audience;
  }

  @Override
  public boolean hasPermission(@NonNull final String permission) {
    return true;
  }

  @Override
  public @NonNull String name() {
    return "Console";
  }

  @Override
  public @Nullable ChatChannel selectedChannel() {
    // TODO: stub
    return null;
  }

  @Override
  public void selectedChannel(@NonNull final ChatChannel channel, final boolean fromRemote) {
    // TODO: stub
  }

  @Override
  public void clearSelectedChannel() {
    // TODO: stub
  }

}
