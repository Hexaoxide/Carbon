package net.draycia.carbon.bukkit;

import cloud.commandframework.CommandManager;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.command.BukkitCommander;
import net.draycia.carbon.bukkit.command.BukkitPlayerCommander;
import net.draycia.carbon.bukkit.users.MemoryUserManagerBukkit;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;

public final class CarbonChatBukkit extends CarbonChatCommon {

  private final @NonNull UserManager userManager = new MemoryUserManagerBukkit();
  private final Logger logger = LogManager.getLogger("CarbonChat");
  private final CarbonChatBukkitEntry plugin;

  CarbonChatBukkit(final @NonNull CarbonChatBukkitEntry plugin) {
    this.plugin = plugin;
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }

  @Override
  public @NonNull UserManager userManager() {
    return this.userManager;
  }

  @Override
  protected @NonNull CommandManager<Commander> createCommandManager() {
    final PaperCommandManager<Commander> commandManager;
    try {
      commandManager = new PaperCommandManager<>(
        this.plugin,
        AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
        commandSender -> {
          if (commandSender instanceof Player) {
            return new BukkitPlayerCommander(this, (Player) commandSender);
          }
          return BukkitCommander.from(commandSender);
        },
        commander -> ((BukkitCommander) commander).commandSender()
      );
    } catch (final Exception ex) {
      throw new RuntimeException("Failed to initialize command manager.", ex);
    }
    commandManager.registerAsynchronousCompletions();
    commandManager.registerBrigadier();
    final CloudBrigadierManager<Commander, ?> brigadierManager = commandManager.brigadierManager();
    if (brigadierManager != null) {
      brigadierManager.setNativeNumberSuggestions(false);
    }
    return commandManager;
  }

  @Override
  public @NonNull Component createItemHoverComponent(final @NonNull UUID uuid) {
    final Player player = Bukkit.getPlayer(uuid); // This is temporary (it's not)

    if (player == null) {
      return empty();
    }

    final ItemStack itemStack;

    final ItemStack mainHand = player.getInventory().getItemInMainHand();

    if (mainHand != null && !mainHand.getType().isAir()) {
      itemStack = mainHand;
    } else {
      final ItemStack offHand = player.getInventory().getItemInMainHand();

      if (offHand != null && !offHand.getType().isAir()) {
        itemStack = offHand;
      } else {
        itemStack = null;
      }
    }

    if (itemStack == null) {
      return empty();
    }

    if (itemStack.getType().isAir()) {
      return empty();
    }

    final Component displayName = requireNonNullElseGet(itemStack.getItemMeta().displayName(), () ->
      translatable(itemStack.getType().getTranslationKey()));

    return createItemHoverComponent(displayName, itemStack);
  }

}
