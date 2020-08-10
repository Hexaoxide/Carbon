package net.draycia.carbon.storage.impl;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.UserChannelSettings;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SimpleUserChannelSettings implements UserChannelSettings {

    private boolean spying;
    private boolean ignored;
    private String color;

    private final UUID uuid;
    private final String channel;

    public SimpleUserChannelSettings(UUID uuid, String channel) {
        this.uuid = uuid;
        this.channel = channel;
    }

    private CarbonChatUser getUser() {
        CarbonChat carbonChat = (CarbonChat)Bukkit.getPluginManager().getPlugin("CarbonChat");

        return (CarbonChatUser)carbonChat.getUserService().wrap(uuid);
    }

    @Override
    public boolean isSpying() {
        return this.spying;
    }

    @Override
    public void setSpying(boolean spying, boolean fromRemote) {
        this.spying = spying;

        if (fromRemote) {
            if (spying) {
                getUser().publishChangeToRedis("spying-channel", channel);
            } else {
                getUser().publishChangeToRedis("unspying-channel", channel);
            }
        }
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public void setIgnoring(boolean ignored, boolean fromRemote) {
        this.ignored = ignored;

        if (fromRemote) {
            if (spying) {
                getUser().publishChangeToRedis("ignoring-channel", channel);
            } else {
                getUser().publishChangeToRedis("unignoring-channel", channel);
            }
        }
    }

    @Override
    public @Nullable TextColor getColor() {
        if (color == null) {
            return null;
        }

        return TextColor.fromHexString(color);
    }

    @Override
    public void setColor(@Nullable TextColor color, boolean fromRemote) {
        if (color == null) {
            this.color = null;
        } else {
            this.color = color.asHexString();
        }

        if (fromRemote) {
            getUser().publishChangeToRedis("channel-color", this.color);
        }
    }

}
