package net.draycia.carbon.common.messages;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.moonshine.message.IMessageRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonMessageRenderer implements IMessageRenderer<Audience, String, Component, Component> {

    @Override
    public Component render(
        final Audience receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        return MiniMessage.get().parse(intermediateMessage, resolvedPlaceholders);
    }

}
