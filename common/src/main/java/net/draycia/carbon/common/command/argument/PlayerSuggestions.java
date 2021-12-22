package net.draycia.carbon.common.command.argument;

import cloud.commandframework.context.CommandContext;
import java.util.List;
import java.util.function.BiFunction;
import net.draycia.carbon.common.command.Commander;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PlayerSuggestions extends BiFunction<CommandContext<Commander>, String, List<String>> {
}
