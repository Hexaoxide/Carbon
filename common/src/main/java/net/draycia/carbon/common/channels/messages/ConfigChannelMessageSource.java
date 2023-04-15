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
package net.draycia.carbon.common.channels.messages;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.util.DiscordRecipient;
import net.kyori.adventure.audience.Audience;
import net.kyori.moonshine.message.IMessageSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class ConfigChannelMessageSource implements IMessageSource<SourcedAudience, String> {

    // Map<String, String> -> Map<Group, Format>
    // "default" key will be configurable but let's not worry about that for now
    @Setting("basic")
    @Comment("""
        Basic chat formats.
        The "default_format" format is the main one you want to edit.
        The "console" format is what's shown to console.
        The "discord" format is what's shown to supported discord integrations.
        If PlaceholderAPI is installed, PAPI placeholders (with %) are supported.
        If MiniPlaceholders is installed, its placeholders (with <>) are supported.
        The keys are group names, the values are chat formats (MiniMessage).
        For example:
            basic {
                default_format="<<username>> <message>"
                vip="[VIP] <<username>> <message>"
                admin="<white>[</white>Prefix<white>]</white> <display_name><white>: <message></white>"
                discord="<message>"
            }
        """)
    private Map<String, String> defaults = Map.of(
        "default_format", "<display_name>: <message>",
        "console", "[<channel>] <username> - <uuid>: <message>",
        "discord", "<message>"
    );

    // TODO: Move the default to the advanced config?
    @Comment("""
        Per-Language chat formats.
        You can safely delete this section if you don't want to use this feature.
        Will fall back to the defaults section if no format was found for the player.
        """)
    private Map<Locale, Map<String, String>> locales = Map.of(
        Locale.US, Map.of("default_format", "<display_name>: <message>")
    );

    private static final String FALLBACK_FORMAT = "<red><</red><username><red>></red> <message>";

    // TODO: Remove DiscordRecipient and use key instead (Couldn't figure out how to do it)
    @Override
    public String messageOf(final SourcedAudience sourcedAudience, final String ignored) {
        if (sourcedAudience.recipient() instanceof CarbonPlayer) {
            return this.forPlayer(sourcedAudience);
        } else if (sourcedAudience.recipient() instanceof DiscordRecipient) {
            return this.defaults.getOrDefault("discord", FALLBACK_FORMAT);
        } else {
            return this.defaults.getOrDefault("console", FALLBACK_FORMAT);
        }
    }

    private String forPlayer(final SourcedAudience sourcedAudience) {
        final var sender = (CarbonPlayer) sourcedAudience.sender();
        final var recipient = (CarbonPlayer) sourcedAudience.recipient();

        if (recipient.locale() != null) {
            final var formats = this.locales.get(recipient.locale());

            if (formats != null) {
                final @Nullable String format = formats.get(sender.primaryGroup());

                if (format != null) {
                    return format;
                }

                for (final var groupEntry : sender.groups()) {
                    final @Nullable String groupFormat = formats.get(groupEntry);

                    if (groupFormat != null) {
                        return groupFormat;
                    }
                }
            }
        }

        final @Nullable String format = this.defaults.get(sender.primaryGroup());

        if (format != null) {
            return format;
        }

        for (final var groupEntry : sender.groups()) {
            final @Nullable String groupFormat = this.defaults.get(groupEntry);

            if (groupFormat != null) {
                return groupFormat;
            }
        }

        return Objects.requireNonNullElse(this.defaults.get("default_format"), FALLBACK_FORMAT);
    }

    private String forAudience(final Audience audience) {
        return Objects.requireNonNullElse(this.defaults.get("console"), FALLBACK_FORMAT);
    }

}
