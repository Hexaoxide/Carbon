package net.draycia.carbon.bukkit.util;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BukkitCapabilities {

    private BukkitCapabilities() {

    }

    private static Boolean vaultEnabled = null;

    public static boolean vaultEnabled() {
        if (vaultEnabled == null) {
            vaultEnabled = Bukkit.getPluginManager().isPluginEnabled("Vault");
        }

        return vaultEnabled;
    }

    private static Permission permission = null;

    public static Permission permission() {
        if (!vaultEnabled()) {
            return null;
        }

        if (permission == null) {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer()
                .getServicesManager().getRegistration(Permission.class);

            permission = rsp.getProvider();
        }

        return permission;
    }

}
