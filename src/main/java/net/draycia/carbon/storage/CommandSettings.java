package net.draycia.carbon.storage;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandSettings {

    private final boolean enabled;

    @NonNull
    private final String name;

    @NonNull
    private final List<@NonNull String> aliases;

    public CommandSettings(boolean enabled, @NonNull String name, @NonNull List<@NonNull String> aliases) {
        this.enabled = enabled;
        this.name = name;
        this.aliases = aliases;
    }

    public @NonNull String @NonNull [] getAliasesArray() {
        return aliases.toArray(new String[0]);
    }

    @NonNull
    public List<@NonNull String> getAliases() {
        return aliases;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
