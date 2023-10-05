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
package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class DeleteMessageCommand extends CarbonCommand {

    private final HashMap<UUID, SignedMessage.Signature> signatures = new HashMap<>();

    private final CarbonChat carbonChat;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public DeleteMessageCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;

        this.carbonChat.eventHandler().subscribe(CarbonChatEvent.class, event -> {
            if (event.signedMessage() != null && event.signedMessage().canDelete()) {
                final UUID uuid = UUID.randomUUID();

                this.signatures.put(uuid, event.signedMessage().signature());

                // TODO: Only show the button to players with permission to delete messages
                // TODO: Configurable button format "[X] <message>"
                // Maybe runCommand instead of suggest? From testing, suggest feels weird to use.
                event.renderers().add(KeyedRenderer.keyedRenderer(Key.key("carbon", "deletemessage"), ((sender, recipient, message, originalMessage) -> {
                    return Component.text("[X] ").clickEvent(ClickEvent.suggestCommand("/deletemessage " + uuid)).append(message);
                })));
            }
        });
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("deletemessage", "deletemsg", "dmsg", "delete");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "deletemessage");
    }

    @Override
    public void init() {
        final var root = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.deletemessage")
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandDeleteDescription());

        // TODO: specify target players
        // TODO: Log who and what message was deleted? You could infer this from other logs but it might be helpful.
        // TODO: MessageDeletedEvent?

        final var specificMessage = root
            .argument(UUIDArgument.of("msg"))
            .handler(handler -> {
                final UUID messageId = handler.get("msg");
                final SignedMessage.Signature signature = this.signatures.get(messageId);

                if (signature == null) {
                    return; // TODO: error message
                }

                for (final CarbonPlayer player : this.carbonChat.server().players()) {
                    player.deleteMessage(signature);
                }
            }).build();

        this.commandManager.command(specificMessage);

        final var allMessages = root
            .literal("all")
            .handler(handler -> {
                for (final SignedMessage.Signature signature : this.signatures.values()) {
                    for (final CarbonPlayer player : this.carbonChat.server().players()) {
                        player.deleteMessage(signature);
                    }
                }
            }).build();

        this.commandManager.command(allMessages);
    }

}
