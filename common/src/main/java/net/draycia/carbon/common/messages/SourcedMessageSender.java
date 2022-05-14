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
package net.draycia.carbon.common.messages;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.moonshine.message.IMessageSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class SourcedMessageSender implements IMessageSender<SourcedAudience, RenderedMessage> {

    @Override
    public void send(final SourcedAudience receiver, final RenderedMessage renderedMessage) {
        if (receiver.sender() instanceof CarbonPlayer sender) {
            receiver.recipient().sendMessage(Identity.identity(sender.uuid()), renderedMessage.component(), renderedMessage.messageType());
        } else {
            receiver.recipient().sendMessage(Identity.nil(), renderedMessage.component(), renderedMessage.messageType());
        }
    }

}
