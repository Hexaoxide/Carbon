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
package net.draycia.carbon.common.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
// TODO: Config versioning. This isn't automatically added to existing configs otherwise.
public class ClearChatSettings {

    @Comment("The message that will be sent to each player.")
    private String message = "";

    @Comment("The number of times the message will be sent to each player.")
    private int iterations = 50;

    @Comment("The message to be sent after chat is cleared.")
    private String broadcast = "<gold>Chat has been cleared by </gold><green><display_name><green><gold>.";

    private @MonotonicNonNull Component messageComponent = null;

    public Component message() {
        if (this.messageComponent == null) {
            this.messageComponent = MiniMessage.miniMessage().deserialize(this.message);
        }

        return this.messageComponent;
    }

    public int iterations() {
        return this.iterations;
    }

    public Component broadcast(final Component displayName, final String username) {
        return MiniMessage.miniMessage().deserialize(this.broadcast,
            TagResolver.builder()
                .tag("display_name", Tag.selfClosingInserting(displayName))
                .tag("username", Tag.selfClosingInserting(Component.text(username)))
                .build());
    }

}
