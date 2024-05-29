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
package net.draycia.carbon.api.channels;

import java.util.List;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.ChatComponentRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Keyed;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * ChatChannel interface, supplies a formatter and filters recipients.<br>
 * Extends Keyed for identification purposes.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface ChatChannel extends Keyed, ChatComponentRenderer {

    /**
     * Checks if the player may send messages in this channel.
     *
     * @param carbonPlayer the player attempting to speak
     * @return if the player may speak
     * @since 3.0.0
     */
    ChannelPermissionResult speechPermitted(CarbonPlayer carbonPlayer);

    /**
     * Checks if the player may receive messages from this channel.
     *
     * @param player the player that's receiving messages
     * @return if the player may receive messages
     * @since 3.0.0
     */
    ChannelPermissionResult hearingPermitted(CarbonPlayer player);

    /**
     * Returns a list of all recipients that will receive messages from the sender.
     *
     * @param sender the sender of messages
     * @return the recipients
     * @since 2.0.0
     */
    List<Audience> recipients(CarbonPlayer sender);

    /**
     * Messages will be sent in this channel if they start with this prefix.
     *
     * @return the message prefix that sends messages in this channel
     * @since 2.0.0
     */
    @Nullable String quickPrefix();

    /**
     * If commands should be registered for this channel.
     *
     * @return if commands should be registered for this channel.
     * @since 2.0.0
     */
    boolean shouldRegisterCommands();

    /**
     * The text that can be used to refer to this channel in commands.
     *
     * @return this channel's name when used in commands
     * @since 2.0.0
     */
    String commandName();

    /**
     * Alternative command names for this channel.
     *
     * @return alternative command names
     * @since 2.0.0
     */
    List<String> commandAliases();

    /**
     * The base permission players must have in order to use the channel.<br>
     * Null return means players do not need any permission.
     *
     * @return the permission required to use the channel, or null
     * @since 2.0.7
     */
    @MonotonicNonNull String permission();

    /**
     * The distance from the sender players must be to receive chat messages.<br>
     * Return of '0' means players must be in the same world/server.<br>
     * Return of '-1' means there is no radius.
     *
     * @return the channel radius
     * @since 3.0.0
     */
    double radius();

    /**
     * If the empty receipt message should be sent to the sender.
     *
     * @return Returns true if the channel should display a message when a player is out of range.
     * @since 3.0.0
     */
    boolean emptyRadiusRecipientsMessage();

    long cooldown();

    long playerCooldown(CarbonPlayer player);

    long startCooldown(CarbonPlayer player);

}
