/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class SoundSettings {

    private Key name = Key.key("entity.arrow.hit_player");
    private Sound.Source source = Sound.Source.MASTER;
    private float volume = 1.0f; // 0.0 -> infinity
    private float pitch = 1.0f; // 0.0 -> 2.0

    public Key name() {
        return this.name;
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
