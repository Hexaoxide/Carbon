package net.draycia.carbon.common.util;

import net.kyori.adventure.audience.Audience;

public class DiscordRecipient implements Audience {
    public static final DiscordRecipient INSTANCE = new DiscordRecipient();

    private DiscordRecipient() {}
}
