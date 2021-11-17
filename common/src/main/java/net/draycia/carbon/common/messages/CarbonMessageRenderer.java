package net.draycia.carbon.common.messages;

import com.google.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.util.ChatType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import net.kyori.moonshine.message.IMessageRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonMessageRenderer implements IMessageRenderer<Audience, String, RenderedMessage, Component> {

    private final PrimaryConfig config;

    @Inject
    public CarbonMessageRenderer(final PrimaryConfig config) {
        this.config = config;
    }

    @Override
    public RenderedMessage render(
        final Audience receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final List<Template> templates = new ArrayList<>();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            templates.add(Template.template(entry.getKey(), entry.getValue()));
        }

        // https://github.com/KyoriPowered/adventure-text-minimessage/issues/131
        // TLDR: 25/10/21, tags in templates aren't parsed. we want them parsed.
        String placeholderResolvedMessage = intermediateMessage;

        for (final var entry : this.config.customPlaceholders().entrySet()) {
            placeholderResolvedMessage = placeholderResolvedMessage.replace("<" + entry.getKey() + ">",
                entry.getValue());
        }

        final Component message = MiniMessage.miniMessage().deserialize(placeholderResolvedMessage, TemplateResolver.templates(templates));
        final MessageType messageType;
        final @Nullable ChatType chatType = method.getAnnotation(ChatType.class);

        if (chatType != null) {
            messageType = chatType.value();
        } else {
            messageType = MessageType.SYSTEM;
        }

        return new RenderedMessage(message, messageType);
    }

}
