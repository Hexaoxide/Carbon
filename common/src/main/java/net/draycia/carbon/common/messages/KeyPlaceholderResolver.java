package net.draycia.carbon.common.messages;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.placeholder.ConclusionValue;
import net.kyori.moonshine.placeholder.ContinuanceValue;
import net.kyori.moonshine.placeholder.IPlaceholderResolver;
import net.kyori.moonshine.util.Either;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KeyPlaceholderResolver<R> implements IPlaceholderResolver<R, Key, Component> {

    @Override
    public @Nullable Map<String, Either<ConclusionValue<? extends Component>, ContinuanceValue<?>>>
    resolve(
        final String placeholderName,
        final Key value,
        final R receiver,
        final Type owner,
        final Method method,
        final @Nullable Object[] parameters
    ) {
        return Map.of(placeholderName, Either.left(ConclusionValue.conclusionValue(Component.text(value.toString()))));
    }

}
