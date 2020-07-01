package net.draycia.simplechat.storage;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public abstract class UserService {

    public abstract ChatUser wrap(OfflinePlayer player);
    public abstract ChatUser wrap(UUID uuid);

}
