package net.draycia.simplechat.managers;

import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserManager {

    private static List<ChatUser> users = new ArrayList<>();

    public static ChatUser wrap(Player player) {
        return wrap(player.getUniqueId());
    }

    public static ChatUser wrap(UUID uuid) {
        for (ChatUser user : users) {
            if (user.getUUID().equals(uuid)) {
                return user;
            }
        }

        return loadUser(uuid);
    }

    private static ChatUser loadUser(UUID uuid) {
        throw new UnsupportedOperationException("Not yet implemented!"); // TODO: implement
    }

}
