package net.draycia.carbon.paper;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.PlatformScheduler;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ContextualPlatformScheduler implements PlatformScheduler {

    private final CarbonChatPaper carbonChat;
    private boolean isFolia;

    public ContextualPlatformScheduler(final CarbonChatPaper carbonChat) {
        this.carbonChat = carbonChat;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            this.isFolia = true;
        } catch (final ClassNotFoundException exception) {
            this.isFolia = false;
        }
    }

    private boolean isFolia() {
        return this.isFolia;
    }

    public void scheduleForPlayer(final CarbonPlayer carbonPlayer, final Runnable runnable) {
        final JavaPlugin plugin = this.carbonChat.bukkitPlugin();

        if (!isFolia()) {
            if (Bukkit.isPrimaryThread()) {
                runnable.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, runnable);
            }
        } else {
            final Player player = ((CarbonPlayerPaper)carbonPlayer).bukkitPlayer();

            if (player == null) {
                return;
            }

            if (Bukkit.isOwnedByCurrentRegion(player)) {
                runnable.run();
            } else {
                player.getScheduler().run(plugin, scheduledTask -> {
                    runnable.run();
                }, null);
            }
        }
    }

}
