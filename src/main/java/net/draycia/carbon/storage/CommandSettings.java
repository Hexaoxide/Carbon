package net.draycia.carbon.storage;

import java.util.List;

public class CommandSettings {

    private boolean enabled;
    private String name;
    private List<String> aliases;

    public CommandSettings(boolean enabled, String name, List<String> aliases) {
        this.enabled = enabled;
        this.name = name;
        this.aliases = aliases;
    }

    public String[] getAliasesArray() {
        return aliases.toArray(new String[0]);
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
