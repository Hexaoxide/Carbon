package net.draycia.carbon.common.channels.messages;

import java.util.Locale;
import java.util.Map;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.moonshine.message.IMessageSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class ConfigChannelMessageSource implements IMessageSource<Audience, String> {

    // Map<String, String> -> Map<Group, Format>
    // "default" key will be configurable but let's not worry about that for now
    @Setting("basic")
    @Comment("""
        Basic chat formats.
        The "default" format is the main one you want to edit.
        The "console" format is what's shown to console.
        The keys are group names, the values are chat formats (MiniMessage).
        """)
    private final Map<String, String> defaults = Map.of("default", "<displayname>: <message>",
        "console", "<username> - <uuid>: <message>");

    // TODO: Move the default to the advanced config?
    @Comment("""
        Per-Language chat formats.
        You can safely delete this section if you don't want to use this feature.
        Will fall back to the defaults section if no format was found for the player.
        """)
    private final Map<Locale, Map<String, String>> locales = Map.of(Locale.US,
        Map.of("default", "<displayname>: <message>"));

    @Override
    public String messageOf(final Audience receiver, final String messageKey) {
        if (receiver instanceof CarbonPlayer player) {
            return this.forPlayer(messageKey, player);
        } else {
            return this.forAudience(messageKey, receiver);
        }
    }

    private String forPlayer(final String key, final CarbonPlayer player) {
        // TODO: rewrite and reimplement
        //        final Map<String, String> formats = this.locales.get(player.locale());
        //
        //        if (formats != null) {
        //            final String format = formats.get(player.primaryGroup());
        //
        //            if (format != null) {
        //                return format;
        //            }
        //
        //            final String defaultFormat = formats.get("default");
        //
        //            if (defaultFormat != null) {
        //                return defaultFormat;
        //            }
        //        }

        final String format = this.defaults.get(player.primaryGroup());

        if (format != null) {
            return format;
        }

        return this.defaults.get("default");
    }

    private String forAudience(final String key, final Audience audience) {
        return this.defaults.get("console");
    }

}
