package net.draycia.simplechat.managers;

import net.draycia.simplechat.SimpleChat;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class DiscordManager {

    private DiscordApi discordAPI = null;

    public DiscordManager(SimpleChat simpleChat) {
        if (simpleChat.getConfig().contains("bot-token") && !simpleChat.getConfig().getString("bot-token").isEmpty()) {
            try {
                discordAPI = new DiscordApiBuilder().setToken(simpleChat.getConfig().getString("bot-token")).login().join();
            } catch (IllegalStateException exception) {
                simpleChat.getLogger().warning("Unable to start bot. Reason: " + exception.getMessage());
                simpleChat.getLogger().warning("If you're getting \"Websocket closed\", check that your bot token is valid!");
            }
        }

    }

    public DiscordApi getDiscordAPI() {
        return discordAPI;
    }
}
