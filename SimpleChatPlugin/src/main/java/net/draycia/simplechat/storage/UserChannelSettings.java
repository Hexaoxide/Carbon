package net.draycia.simplechat.storage;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

public interface UserChannelSettings {

    boolean isSpying();
    void setSpying(boolean spying);

    boolean isIgnored();
    void setIgnoring(boolean ignored);

    @Nullable TextColor getColor();
    void setColor(@Nullable TextColor color);

}
