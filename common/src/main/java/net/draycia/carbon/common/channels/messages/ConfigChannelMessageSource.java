package net.draycia.carbon.common.channels.messages;

import com.proximyst.moonshine.message.IMessageSource;
import java.util.Locale;
import java.util.Map;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class ConfigChannelMessageSource implements IMessageSource<String, Audience> {

    // Map<String, String> -> Map<Group, Format>
    // "default" key will be configurable but let's not worry about that for now
    @Comment("")
    private Map<String, String> defaults = Map.of("default", "<displayname>: <message>",
        "console", "<username> - <uuid>: <message>");

    // TODO: This needs to be empty by default
    // TODO: New version of this class, for making configs
    @Comment("")
    private Map<Locale, Map<String, String>> locales = Map.of(Locale.US,
        Map.of("default", "<displayname>: <message>"));

    @Override
    public String message(final String key, final Audience receiver) {
        if (receiver instanceof CarbonPlayer player) {
            return this.forPlayer(key, player);
        } else {
            return this.forAudience(key, receiver);
        }
    }

    private String forPlayer(final String key, final CarbonPlayer player) {
        System.out.println(player.locale());
        final Map<String, String> formats = this.locales.get(player.locale());

        if (formats != null) {
            final String format = formats.get(player.primaryGroup());

            if (format != null) {
                return format;
            }

            final String defaultFormat = formats.get("default");

            if (defaultFormat != null) {
                return defaultFormat;
            }
        }

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
