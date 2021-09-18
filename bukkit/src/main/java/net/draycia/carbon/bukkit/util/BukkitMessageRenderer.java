package net.draycia.carbon.bukkit.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.moonshine.message.IMessageRenderer;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class BukkitMessageRenderer implements IMessageRenderer<SourcedAudience, String, Component, Component> {

    private final PlaceholderAPIMiniMessageParser parser = PlaceholderAPIMiniMessageParser.create(MiniMessage.get());

    @Override
    public Component render(
        final SourcedAudience receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final List<Template> templates = new ArrayList<>();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            templates.add(Template.of(entry.getKey(), entry.getValue()));
        }

        if (receiver.sender() instanceof CarbonPlayer sender && sender.online()) {
            if (receiver.recipient() instanceof CarbonPlayer recipient && recipient.online()) {
                return this.parser.parseRelational(Bukkit.getPlayer(sender.uuid()),
                    Bukkit.getPlayer(recipient.uuid()), intermediateMessage, templates);
            } else {
                return this.parser.parse(Bukkit.getPlayer(sender.uuid()), intermediateMessage, templates);
            }
        }

        return MiniMessage.get().parse(intermediateMessage, templates);
    }

}
