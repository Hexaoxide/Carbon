/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.channels;

import java.util.Collections;
import java.util.List;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ConfigChannelSettings {

    @Comment("""
        The channel's key, used to track the channel.
        You only need to change the second part of the key. "global" by default.
        The value is what's used in commands, this is probably what you want to change.
        """)
    private @Nullable Key key = Key.key("carbon", "basic");

    @Comment("""
        The permission required to use the channel.
        To read messages you must have the permission carbon.channel.basic.see
        To send messages you must have the permission carbon.channel.basic.speak
        If you want to give both, grant carbon.channel.basic or carbon.channel.basic.*
        """)
    private @Nullable String permission = "carbon.channel.basic";

    @Setting("format")
    @Comment("The chat formats for this channel.")
    private @Nullable ConfigChannelMessageSource messageSource = new ConfigChannelMessageSource();

    @Comment("Messages will be sent in this channel if they start with this prefix.")
    private @Nullable String quickPrefix = "";

    private @Nullable Boolean shouldRegisterCommands = true;

    private @Nullable String commandName = null;

    private @Nullable List<String> commandAliases = Collections.emptyList();

}
