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
package net.draycia.carbon.common.messages;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonMessageSender implements IMessageSender<Audience, Component> {

    @Override
    public void send(final Audience receiver, final Component renderedMessage) {
        if (receiver instanceof SourcedAudience sourcedAudience) {
            if (sourcedAudience.sender() instanceof CarbonPlayer sender && !sender.hasPermission("carbon.hideidentity")) {
                sourcedAudience.recipient().sendMessage(Identity.identity(sender.uuid()), renderedMessage);
            } else {
                sourcedAudience.recipient().sendMessage(Identity.nil(), renderedMessage);
            }
        } else {
            receiver.sendMessage(Identity.nil(), renderedMessage);
        }
    }

}
