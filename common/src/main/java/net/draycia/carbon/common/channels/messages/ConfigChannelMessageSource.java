package net.draycia.carbon.common.channels.messages;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
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
        The keys are group names, the values are chat formats (MiniMessage).
        For example:
            basic {
                default_format="<<username>> <message>"
                vip="[VIP] <<username>> <message>"
                admin="<white>[</white>%luckperms_prefix%<white>]</white> <display_name><white>: <message></white>"
            }
        """)
    private final Map<String, String> defaults = Map.of(
        "default_format", "<display_name>: <message>",
        "console", "[<channel>] <username> - <uuid>: <message>"
    );

    // TODO: Move the default to the advanced config?
    @Comment("""
        Per-Language chat formats.
        You can safely delete this section if you don't want to use this feature.
        Will fall back to the defaults section if no format was found for the player.
        """)
    private final Map<Locale, Map<String, String>> locales = Map.of(
        Locale.US, Map.of("default_format", "<display_name>: <message>")
    );

    private static final String FALLBACK_FORMAT = "<red><</red><username><red>></red> <message>";

    @Override
    public String messageOf(final SourcedAudience sourcedAudience, final String messageKey) {
        if (sourcedAudience.recipient() instanceof CarbonPlayer) {
            return this.forPlayer(messageKey, sourcedAudience);
        } else {
            return this.forAudience(messageKey, sourcedAudience.recipient());
        }
    }

    private String forPlayer(final String key, final SourcedAudience sourcedAudience) {
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

    private String forAudience(final String key, final Audience audience) {
        return Objects.requireNonNullElse(this.defaults.get("console"), FALLBACK_FORMAT);
    }

}
