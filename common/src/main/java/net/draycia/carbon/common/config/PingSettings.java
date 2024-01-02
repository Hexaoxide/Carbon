/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class PingSettings {

    @Comment("The color your name will be when another player mentions you.")
    private TextColor highlightTextColor = NamedTextColor.YELLOW;

    private String prefix = "@";

    private boolean playSound = false;
    private Key name = Key.key("block.anvil.use");
    private Sound.Source source = Sound.Source.MASTER;
    private float volume = 1.0f; // 0.0 -> infinity
    private float pitch = 1.0f; // 0.0 -> 2.0

    public TextColor highlightTextColor() {
        return this.highlightTextColor;
    }

    public boolean playSound() {
        return this.playSound;
    }

    public Key name() {
        return this.name;
    }

    public String prefix() {
        return this.prefix;
    }

    public Sound.Source source() {
        return this.source;
    }

    public float volume() {
        return this.volume;
    }

    public float pitch() {
        return this.pitch;
    }

    public Sound sound() {
        return Sound.sound(this.name, this.source, this.volume, this.pitch);
    }

}
