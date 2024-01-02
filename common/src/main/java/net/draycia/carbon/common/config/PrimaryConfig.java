/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
import net.draycia.carbon.common.util.Exceptions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PrimaryConfig {

    @Comment("The default locale for plugin messages.")
    private Locale defaultLocale = Locale.US;

    @Comment("""
        The default channel that new players will be in when they join.
        If the channel is not found or the player cannot use the channel, they will speak in basic non-channel chat.""")
    private Key defaultChannel = Key.key("carbon", "global");

    @Comment("""
        The service that will be used to store and load player information.
        One of: JSON, H2, MYSQL, PSQL
        Note: If you choose MYSQL or PSQL make sure you configure the "database-settings" section of this file!""")
    private StorageType storageType = StorageType.JSON;

    @Comment("""
        When "storage-type" is set to MYSQL or PSQL, this section configures the database connection.
        If JSON or H2 storage is used, this section can be ignored.""")
    private DatabaseSettings databaseSettings = new DatabaseSettings();

    @Comment("Settings for cross-server messaging")
    private MessagingSettings messagingSettings = new MessagingSettings();

    private NicknameSettings nicknameSettings = new NicknameSettings();

    @Comment("""
        Plugin-wide custom placeholders.
        These will be parsed in all messages rendered and sent by Carbon.
        This includes chat, command feedback, and others.
        Make sure to close your tags so they do not bleed into other formats.
        Only a single pass is done so custom placeholders will not work within each other.""")
    private Map<String, String> customPlaceholders = Map.of();

    @Comment("The suggestions shown when using the TAB key in chat.")
    private List<String> customChatSuggestions = List.of();

    @Comment("The placeholders replaced in chat messages, this WILL work with chat previews.")
    private Map<String, String> chatPlaceholders = Map.of();

    @Comment("Basic regex based chat filter.")
    private Map<String, String> chatFilter = Map.of();

    @Comment("Various settings related to pinging players in channels.")
    private PingSettings pingSettings = new PingSettings();

    private PartySettings partyChat = new PartySettings();

    @Comment("Sound for receiving a direct message") // TODO migrate to a field name that makes more sense
    private @Nullable Sound messageSound = Sound.sound(
        Key.key("entity.experience_orb.pickup"),
        Sound.Source.MASTER,
        1.0F,
        1.0F
    );

    @Comment("Settings for the clear chat command")
    private ClearChatSettings clearChatSettings = new ClearChatSettings();

    @Comment("Settings for integrations with other plugins/mods. Settings only apply when the relevant plugin/mod is present.")
    private IntegrationConfigContainer integrations;

    @Comment("Whether Carbon should check for updates using the GitHub API on startup.")
    private boolean updateChecker = true;

    public NicknameSettings nickname() {
        return this.nicknameSettings;
    }

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

    public MessagingSettings messagingSettings() {
        return this.messagingSettings;
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

    public String applyChatPlaceholders(final String string) {
        String placeholderResolvedMessage = string;
        for (final var entry : this.chatPlaceholders.entrySet()) {
            placeholderResolvedMessage = placeholderResolvedMessage.replace("<" + entry.getKey() + ">",
                entry.getValue());
        }
        return placeholderResolvedMessage;
    }

    public String applyChatFilters(final String string) {
        String filteredMessage = string;

        for (final Map.Entry<String, String> entry : this.chatFilter.entrySet()) {
            filteredMessage = filteredMessage.replaceAll(entry.getKey(), entry.getValue());
        }

        return filteredMessage;
    }

    public PingSettings pings() {
        return this.pingSettings;
    }

    public PartySettings partyChat() {
        return this.partyChat;
    }

    public @Nullable Sound messageSound() {
        return this.messageSound;
    }

    public ClearChatSettings clearChatSettings() {
        return this.clearChatSettings;
    }

    public IntegrationConfigContainer integrations() {
        return this.integrations;
    }

    public boolean updateChecker() {
        return this.updateChecker;
    }

    @SuppressWarnings("unused")
    public static void upgrade(final ConfigurationNode node) {
        final ConfigurationTransformation.VersionedBuilder builder = ConfigurationTransformation.versionedBuilder()
            .versionKey(ConfigManager.CONFIG_VERSION_KEY);
        final ConfigurationTransformation initial = ConfigurationTransformation.builder()
            .addAction(NodePath.path("use-carbon-nicknames"), (path, value) -> new Object[]{"nickname-settings", "use-carbon-nicknames"})
            .build();
        builder.addVersion(0, initial);
        final ConfigurationTransformation one = ConfigurationTransformation.builder()
            .addAction(NodePath.path("party-chat"), (path, value) -> new Object[]{"party-chat", "enabled"})
            .build();
        builder.addVersion(1, one);
        final ConfigurationTransformation.Versioned upgrader = builder.build();
        final int from = upgrader.version(node);
        try {
            upgrader.apply(node);
        } catch (final ConfigurateException e) {
            Exceptions.rethrow(e);
        }

        ConfigManager.configVersionComment(node, upgrader);
    }

    @ConfigSerializable
    public static final class NicknameSettings {

        @Comment("Whether Carbon's nickname management should be used. Disable this if you wish to have another plugin manage nicknames.")
        private boolean useCarbonNicknames = true;

        @Comment("Minimum number of characters in nickname (excluding formatting).")
        private int minLength = 3;

        @Comment("Maximum number of characters in nickname (excluding formatting).")
        private int maxLength = 16;

        @Comment("Format used when displaying nicknames.")
        public String format = "<hover:show_text:'<gray>@</gray><username>'><gray>~</gray><nickname></hover>";

        @Comment("Whether to skip applying 'format' when a nickname matches a players username, only differing in decoration.")
        public boolean skipFormatWhenNameMatches = true;

        public boolean useCarbonNicknames() {
            return this.useCarbonNicknames;
        }

        public int minLength() {
            return this.minLength;
        }

        public int maxLength() {
            return this.maxLength;
        }
    }

    @ConfigSerializable
    public static final class PartySettings {

        @Comment("Whether party chat is enabled")
        public boolean enabled = true;

        public int expireInvitesAfterSeconds = 45;
    }

    public enum StorageType {
        JSON,
        MYSQL,
        PSQL,
        H2
    }

}
