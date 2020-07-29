package net.draycia.carbon.storage.impl;

import net.draycia.carbon.storage.UserChannelSettings;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

public class SimpleUserChannelSettings implements UserChannelSettings {

    private boolean spying;
    private boolean ignored;
    private String color;

    @Override
    public boolean isSpying() {
        return this.spying;
    }

    @Override
    public void setSpying(boolean spying) {
        this.spying = spying;
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public void setIgnoring(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public @Nullable TextColor getColor() {
        if (color == null) {
            return null;
        }

        return TextColor.fromHexString(color);
    }

    @Override
    public void setColor(@Nullable TextColor color) {
        if (color == null) {
            this.color = null;
            return;
        }

        this.color = color.asHexString();
    }

}
