package net.draycia.carbon.bukkit.users;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.draycia.carbon.bukkit.util.BukkitCapabilities;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerBukkit extends WrappedCarbonPlayer implements ForwardingAudience.Single {

    private final CarbonPlayerCommon carbonPlayerCommon;

    public CarbonPlayerBukkit(final CarbonPlayerCommon carbonPlayerCommon) {
        this.carbonPlayerCommon = carbonPlayerCommon;
    }

    private @Nullable Player player() {
        return Bukkit.getPlayer(this.carbonPlayerCommon.uuid());
    }

    @Override
    public CarbonPlayerCommon carbonPlayerCommon() {
        return this.carbonPlayerCommon;
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
    public void displayName(final @Nullable Component displayName) {
        this.carbonPlayerCommon.displayName(displayName);

        final @Nullable Player player = this.player();

        if (player != null) {
            // TODO: don't run this block when the player has a temporary display name already set
            // Update player's name in chat
            player.displayName(displayName);

            // Update player's name in the tab player list
            player.playerListName(displayName);
        }
    }

    @Override
    public void temporaryDisplayName(final @Nullable Component displayName, final long expirationEpoch) {
        this.carbonPlayerCommon.temporaryDisplayName(displayName, expirationEpoch);

        final @Nullable Player player = this.player();

        if (player != null) {
            // Update player's name in chat
            player.displayName(displayName);

            // Update player's name in the tab player list
            player.playerListName(displayName);

            // TODO: schedule task to unset temporary display name when it expires
        }
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
    public @Nullable Locale locale() {
        final @Nullable Player player = this.player();

        if (player != null) {
            return player.locale();
        } else {
            return null;
        }
    }

    @Override
    public void sendMessageAsPlayer(final String message) {
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("CarbonChat"), () -> {
            Objects.requireNonNull(this.player()).chat(message);
        });
    }

    @Override
    public boolean online() {
        return this.player() != null;
    }

}
