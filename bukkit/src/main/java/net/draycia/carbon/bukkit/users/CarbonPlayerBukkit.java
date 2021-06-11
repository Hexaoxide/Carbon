package net.draycia.carbon.bukkit.users;

import java.util.Locale;
import java.util.UUID;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerBukkit extends CarbonPlayerCommon implements ForwardingAudience.Single {

    private final CarbonPlayer carbonPlayer;

    public CarbonPlayerBukkit(final CarbonPlayer carbonPlayer) {
        this.carbonPlayer = carbonPlayer;
    }

    public CarbonPlayerBukkit(
        final String username,
        final UUID uuid
    ) {
        super(username, uuid);
        this.carbonPlayer = this;
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        super.displayName(displayName);

        final @Nullable Player player = this.player();

        if (player != null) {
            player.displayName(displayName);
            player.playerListName(displayName);
        }
    }

    @Override
    public @NonNull Audience audience() {
        final @Nullable Player player = this.player();

        if (player == null) {
            return Audience.empty();
        }

        return player;
    }

    @Override
    protected CarbonPlayer carbonPlayer() {
        return this.carbonPlayer;
    }

    @Override
    public @NonNull Component createItemHoverComponent() {
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
    public @Nullable Locale locale() {
        final @Nullable Player player = this.player();

        if (player != null) {
            return player.locale();
        } else {
            return null;
        }
    }

    private @Nullable Player player() {
        return Bukkit.getPlayer(this.uuid);
    }

}
