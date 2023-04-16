/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PrimaryConfig {

    @Comment("The default locale for plugin messages.")
    private Locale defaultLocale = Locale.US;

    @Comment("""
    The default channel that new players will be in when they join.
    If the channel is not found or the player cannot use the channel, they will speak in basic non-channel chat.
    """)
    private Key defaultChannel = Key.key("carbon", "global");

    @Comment("The service that will be used to store and load player information.")
    private StorageType storageType = StorageType.JSON;

    @Comment("")
    private DatabaseSettings databaseSettings = new DatabaseSettings();

    @Comment("Various ClearChat command settings.")
    private ClearChatSettings clearChatSettings = new ClearChatSettings();

    @Comment("""
    Plugin-wide custom placeholders.
    These will be parsed in all messages rendered and sent by Carbon.
    This includes chat, command feedback, and others.
    Make sure to close your tags so they do not bleed into other formats.
    Only a single pass is done so custom placeholders will not work within each other.
    """)
    private Map<String, String> customPlaceholders = Map.of();

    @Comment("The suggestions shown when using the TAB key in chat.")
    private List<String> customChatSuggestions = List.of();

    @Comment("The placeholders replaced in chat messages, this WILL work with chat previews.")
    private Map<String, String> chatPlaceholders = Map.of();

    @Comment("Various settings related to pinging players in channels.")
    private PingSettings pingSettings = new PingSettings();

    @Comment("Various sound settings for messages.")
    private @Nullable Sound messageSound = Sound.sound(
        Key.key("entity.experience_orb.pickup"),
        Sound.Source.MASTER,
        1.0F,
        1.0F
    );

    private MessagingSettings messagingSettings = new MessagingSettings();

    public Locale defaultLocale() {
        return this.defaultLocale;
    }

    public Key defaultChannel() {
        return this.defaultChannel;
    }

    public StorageType storageType() {
        return this.storageType;
    }

    public DatabaseSettings databaseSettings() {
        return this.databaseSettings;
    }

    public ClearChatSettings clearChatSettings() {
        return this.clearChatSettings;
    }

    public Map<String, String> customPlaceholders() {
        return this.customPlaceholders;
    }

    public String applyCustomPlaceholders(final String string) {
        String placeholderResolvedMessage = string;
        for (final var entry : this.customPlaceholders.entrySet()) {
            placeholderResolvedMessage = placeholderResolvedMessage.replace("<" + entry.getKey() + ">",
                entry.getValue());
        }
        return placeholderResolvedMessage;
    }

    public @Nullable List<String> customChatSuggestions() {
        return this.customChatSuggestions;
    }

    public @Nullable Map<String, String> chatPlaceholders() {
        return this.chatPlaceholders;
    }

    public PingSettings pings() {
        return this.pingSettings;
    }

    public MessagingSettings messagingSettings() {
        return this.messagingSettings;
    }

    public @Nullable Sound messageSound() {
        return this.messageSound;
    }

    public enum StorageType {
        JSON,
        MYSQL,
        PSQL
    }

}
