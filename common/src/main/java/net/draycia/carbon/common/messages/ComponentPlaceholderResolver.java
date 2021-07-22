package net.draycia.carbon.common.messages;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.placeholder.ConclusionValue;
import net.kyori.moonshine.placeholder.ContinuanceValue;
import net.kyori.moonshine.placeholder.IPlaceholderResolver;
import net.kyori.moonshine.util.Either;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ComponentPlaceholderResolver<R> implements IPlaceholderResolver<R, Component, Component> {

    @Override
    public @Nullable Map<String, Either<ConclusionValue<? extends Component>, ContinuanceValue<?>>>
    resolve(
        final String placeholderName,
        final Component value,
        final R receiver,
        final Type owner,
        final Method method,
        final @Nullable Object[] parameters
    ) {
        return Map.of(placeholderName, Either.left(ConclusionValue.conclusionValue(value)));
    }

}
