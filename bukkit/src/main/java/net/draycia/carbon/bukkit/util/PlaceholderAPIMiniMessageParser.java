package net.draycia.carbon.bukkit.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Collections.emptyList;

@DefaultQualifier(NonNull.class)
public final class PlaceholderAPIMiniMessageParser {

    private final MiniMessage miniMessage;

    private PlaceholderAPIMiniMessageParser(final MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
    }

    public static PlaceholderAPIMiniMessageParser create(final MiniMessage backingInstance) {
        return new PlaceholderAPIMiniMessageParser(backingInstance);
    }

    private static boolean containsLegacyColorCodes(final String string) {
        final char[] charArray = string.toCharArray();

        for (final char c : charArray) {
            if (c == LegacyComponentSerializer.SECTION_CHAR) {
                return true;
            }
        }

        return false;
    }

    public Component parse(final OfflinePlayer player, final String input, final Collection<Template> templates) {
        return this.parse(
            PlaceholderAPI.getPlaceholderPattern(),
            match -> PlaceholderAPI.setPlaceholders(player, match),
            input,
            templates
        );
    }

    public Component parse(final OfflinePlayer player, final String input) {
        return this.parse(player, input, emptyList());
    }

    public Component parseRelational(final Player one, final Player two, final String input, final Collection<Template> templates) {
        return this.parse(
            PlaceholderAPI.getPlaceholderPattern(),
            match -> PlaceholderAPI.setPlaceholders(one, PlaceholderAPI.setRelationalPlaceholders(one, two, match)),
            input,
            templates
        );
    }

    public Component parseRelational(final Player one, final Player two, final String input) {
        return this.parseRelational(one, two, input, emptyList());
    }

    private Component parse(
        final Pattern pattern,
        final UnaryOperator<String> placeholderResolver,
        final String input,
        final Collection<Template> originalTemplates
    ) {
        final Matcher matcher = pattern.matcher(input);
        final List<Template> templates = new ArrayList<>(originalTemplates);
        final StringBuilder builder = new StringBuilder();
        int id = 0;

        while (matcher.find()) {
            final String match = matcher.group();
            final String replaced = placeholderResolver.apply(match);

            if (match.equals(replaced) || !containsLegacyColorCodes(replaced)) {
                matcher.appendReplacement(builder, replaced);
            } else {
                final String key = "papi_generated_template_" + id;
                id++;
                templates.add(Template.template(key, LegacyComponentSerializer.legacySection().deserialize(replaced)));
                matcher.appendReplacement(builder, "<" + key + ">");
            }
        }

        matcher.appendTail(builder);

        return this.miniMessage.deserialize(builder.toString(), TemplateResolver.templates(templates));
    }

}
