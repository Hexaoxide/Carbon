package net.draycia.carbon.common.messages;

import com.google.common.collect.Multimap;
import com.proximyst.moonshine.component.placeholder.IPlaceholderResolver;
import com.proximyst.moonshine.component.placeholder.PlaceholderContext;
import com.proximyst.moonshine.component.placeholder.ResolveResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ComponentPlaceholderResolver<R> implements IPlaceholderResolver<R, Component> {

    public ResolveResult resolve(
        final String placeholderName,
        final Component value,
        final PlaceholderContext<R> ctx,
        final Multimap<String, @Nullable Object> flags
    ) {
        return ResolveResult.ok(placeholderName, MiniMessage.get().serialize(value));
    }

}
