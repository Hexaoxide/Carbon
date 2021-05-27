package net.draycia.carbon.common.messages;

import com.google.common.collect.Multimap;
import com.proximyst.moonshine.component.placeholder.IPlaceholderResolver;
import com.proximyst.moonshine.component.placeholder.PlaceholderContext;
import com.proximyst.moonshine.component.placeholder.ResolveResult;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UUIDPlaceholderResolver<R> implements IPlaceholderResolver<R, UUID> {

    public ResolveResult resolve(
        final String placeholderName,
        final UUID value,
        final PlaceholderContext<R> ctx,
        final Multimap<String, @Nullable Object> flags
    ) {
        return ResolveResult.ok(placeholderName, value.toString());
    }

}
