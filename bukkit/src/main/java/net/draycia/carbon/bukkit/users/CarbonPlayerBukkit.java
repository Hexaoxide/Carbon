package net.draycia.carbon.bukkit.users;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.bukkit.util.BukkitCapabilities;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerBukkit extends CarbonPlayerCommon implements ForwardingAudience.Single {

    private final CarbonPlayer carbonPlayer;
    private @MonotonicNonNull Scoreboard scoreboard = null;

    public CarbonPlayerBukkit(final CarbonPlayer carbonPlayer) {
        this.carbonPlayer = carbonPlayer;
    }

    private @NonNull Scoreboard scoreboard() {
        if (this.scoreboard == null) {
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        return this.scoreboard;
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.carbonPlayer.displayName(displayName);

        final @Nullable Player player = this.player();

        if (player != null) {
            // Update player's name in chat
            player.displayName(displayName);

            // Update player's name in the tab player list
            player.playerListName(displayName);
        }
    }

    @Override
    public void temporaryDisplayName(@Nullable Component displayName) {
        this.carbonPlayer.temporaryDisplayName(displayName);

        final @Nullable Player player = this.player();

        if (player != null) {
            // Update player's name in chat
            player.displayName(displayName);

            // Update player's name in the tab player list
            player.playerListName(displayName);
        }
    }

    @Override
    public String username() {
        return this.carbonPlayer.username();
    }

    @Override
    public boolean hasCustomDisplayName() {
        return this.carbonPlayer.hasCustomDisplayName();
    }

    @Override
    public @Nullable Component displayName() {
        //        if (this.carbonPlayer.displayName() != null) {
        //            return this.carbonPlayer.displayName();
        //        }

        //        final @Nullable Player player = this.player();
        //
        //        if (player != null) {
        //            return player.displayName();
        //        }

        //        return null;
        return this.carbonPlayer.displayName();
    }

    @Override
    public UUID uuid() {
        return this.carbonPlayer.uuid();
    }

    @Override
    public @NotNull Audience audience() {
        final @Nullable Player player = this.player();

        if (player == null) {
            return Audience.empty();
        }

        return player;
    }

    @Override
    public CarbonPlayer carbonPlayer() {
        return this.carbonPlayer;
    }

    @Override
    public Component createItemHoverComponent() {
        final @Nullable Player player = this.player(); // This is temporary (it's not)

        if (player == null) {
            return Component.empty();
        }

        final @Nullable ItemStack itemStack;

        final @Nullable ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (mainHand != null && !mainHand.getType().isAir()) {
            itemStack = mainHand;
        } else {
            final @Nullable ItemStack offHand = player.getInventory().getItemInOffHand();

            if (offHand != null && !offHand.getType().isAir()) {
                itemStack = offHand;
            } else {
                itemStack = null;
            }
        }

        if (itemStack == null) {
            return Component.empty();
        }

        if (itemStack.getType().isAir()) {
            return Component.empty();
        }

        return itemStack.displayName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        final @Nullable Player player = this.player();

        if (player != null) {
            return player.hasPermission(permission);
        }

        return false;
    }

    @Override
    public String primaryGroup() {
        if (!BukkitCapabilities.vaultEnabled()) {
            return "default";
        }

        final Permission permission = Objects.requireNonNull(BukkitCapabilities.permission());
        final String group = permission.getPrimaryGroup(this.player());

        return Objects.requireNonNullElse(group, "default");
    }

    @Override
    public List<String> groups() {
        if (!BukkitCapabilities.vaultEnabled()) {
            return List.of("default");
        }

        final Permission permission = Objects.requireNonNull(BukkitCapabilities.permission());
        final String[] groups = permission.getPlayerGroups(this.player());

        if (groups != null && groups.length != 0) {
            return Arrays.asList(groups);
        }

        return List.of("default"); // TODO: implement
    }

    @Override
    public boolean muted() {
        return this.carbonPlayer.muted();
    }

    @Override
    public void muted(final boolean muted) {
        this.carbonPlayer.muted(muted);
    }

    @Override
    public boolean deafened() {
        return this.carbonPlayer.deafened();
    }

    @Override
    public void deafened(final boolean deafened) {
        this.carbonPlayer.deafened(deafened);
    }

    @Override
    public boolean spying() {
        return this.carbonPlayer.spying();
    }

    @Override
    public void spying(final boolean spying) {
        this.carbonPlayer.spying(spying);
    }

    @Override
    public @Nullable Locale locale() {
        final @Nullable Player player = this.player();

        if (player != null) {
            return player.locale();
        } else {
            return null;
        }
    }

    private @Nullable Player player() {
        return Bukkit.getPlayer(this.carbonPlayer.uuid());
    }

    @Override
    public @Nullable ChatChannel selectedChannel() {
        return this.carbonPlayer.selectedChannel();
    }

    @Override
    public void selectedChannel(final ChatChannel chatChannel) {
        this.carbonPlayer.selectedChannel(chatChannel);
    }

    @Override
    public Identity identity() {
        return this.carbonPlayer.identity();
    }

}
