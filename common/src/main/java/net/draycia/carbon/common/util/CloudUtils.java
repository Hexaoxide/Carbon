package net.draycia.carbon.common.util;

import java.util.LinkedList;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CloudUtils {

    private static final Component NULL = Component.text("null");

    private CloudUtils() {

    }

    public static Component message(final Throwable throwable) {
        final @Nullable Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

    public static String rawInputByMatchingName(
        final LinkedList<String> rawInput,
        final CarbonPlayer recipient
    ) {
        return rawInput
            .stream()
            .filter(it -> it.equalsIgnoreCase(recipient.username()))
            .findFirst()
            .orElse(recipient.username());
    }

}
