package net.draycia.carbon.bukkit.util;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class BukkitCapabilities {

    private static @Nullable Boolean vaultEnabled = null;
    private static @Nullable Permission permission = null;
    private static @Nullable Chat chat = null;

    private BukkitCapabilities() {

    }

    public static boolean vaultEnabled() {
        if (vaultEnabled == null) {
            vaultEnabled = Bukkit.getPluginManager().isPluginEnabled("Vault");
        }

        return vaultEnabled;
    }

    public static @Nullable Permission permission() {
        if (!vaultEnabled()) {
            return null;
        }

        if (permission == null) {
            final @Nullable RegisteredServiceProvider<Permission> rsp = Bukkit.getServer()
                .getServicesManager().getRegistration(Permission.class);

            if (rsp != null) {
                permission = rsp.getProvider();
            }
        }

        return permission;
    }

    public static @Nullable Chat chat() {
        if (!vaultEnabled()) {
            return null;
        }

        if (chat == null) {
            final @Nullable RegisteredServiceProvider<Chat> rsp = Bukkit.getServer()
                .getServicesManager().getRegistration(Chat.class);

            if (rsp != null) {
                chat = rsp.getProvider();
            }
        }

        return chat;
    }

}
