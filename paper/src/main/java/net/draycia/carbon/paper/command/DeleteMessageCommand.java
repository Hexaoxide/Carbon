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
package net.draycia.carbon.paper.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;

@DefaultQualifier(NonNull.class)
public class DeleteMessageCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final ConfigFactory configFactory;
    final CarbonMessages carbonMessages;

    final Cache<UUID, SignedMessage> signatureCache = Caffeine.newBuilder()
        .maximumSize(50)
        .build();

    @Inject
    public DeleteMessageCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final ConfigFactory configFactory,
        final CarbonMessages carbonMessages
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.configFactory = configFactory;
        this.carbonMessages = carbonMessages;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("deletemessage");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "deletemessage");
    }

    @Override
    public void init() {
        this.carbonChat.eventHandler().subscribe(CarbonChatEvent.class, event -> {
            final SignedMessage signedMessage = event.signedMessage();

            if (signedMessage == null) {
                return;
            }

            final UUID messageId = UUID.randomUUID();

            this.signatureCache.put(messageId, signedMessage);

            event.renderers().add(keyedRenderer(Key.key("carbon", "delete"), (sender, recipient, message, originalMessage) -> {
                if (recipient instanceof CarbonPlayer playerRecipient) {
                    if (!playerRecipient.hasPermission("carbon.deletemessage.message")) {
                        return message;
                    }
                }

                final Component prefix = this.carbonMessages.deleteMessagePrefix()
                    .clickEvent(ClickEvent.runCommand("/deletemessage " + messageId));

                return prefix.append(message);
            }));
        });

        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(UUIDArgument.of("messageId"))
            .permission("carbon.deletemessage.delete")
            .senderType(Commander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandClearChatDescription())
            .handler(handler -> {
                final UUID messageId = handler.get("messageId");
                final @Nullable SignedMessage signature = this.signatureCache.getIfPresent(messageId);

                if (signature == null) {
                    // TODO: error message
                    return;
                }

                for (final CarbonPlayer player : this.carbonChat.server().players()) {
                    player.deleteMessage(signature);
                }

                this.signatureCache.invalidate(messageId);
            })
            .build();

        this.commandManager.command(command);
    }

}
