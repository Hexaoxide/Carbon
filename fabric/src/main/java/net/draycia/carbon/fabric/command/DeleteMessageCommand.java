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
package net.draycia.carbon.fabric.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.fabric.argument.server.SinglePlayerSelectorArgument;
import cloud.commandframework.fabric.data.SinglePlayerSelector;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.types.tuples.Pair;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.draycia.carbon.fabric.mixin.MessageSignatureCacheAccessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.fabric.impl.accessor.minecraft.network.ServerGamePacketListenerImplAccess;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class DeleteMessageCommand extends CarbonCommand {

    final CarbonChatFabric carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages messageService;
    final PlayerSuggestions playerSuggestions;

    @Inject
    public DeleteMessageCommand(
        final CarbonChatFabric carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages messageService,
        final PlayerSuggestions playerSuggestions
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.messageService = messageService;
        this.playerSuggestions = playerSuggestions;
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
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(SinglePlayerSelectorArgument.of("player"))
            .argument(this.commandManager.argumentBuilder(MessageSignature.class, "message")
                .withParser((context, inputQueue) -> {
                    final ServerPlayer target = target(context);
                    final MessageSignatureCache messageSignatureCache = messageSignatureCache(target);

                    return Arrays.stream(((MessageSignatureCacheAccessor) messageSignatureCache).access$entries())
                        .filter(Objects::nonNull)
                        .map(e -> Pair.of(e, UUID.nameUUIDFromBytes(e.bytes())))
                        .filter(s -> s.getSecond().toString().equals(inputQueue.peek()))
                        .findFirst()
                        .map(pair -> {
                            inputQueue.poll();
                            return ArgumentParseResult.success(pair.getFirst());
                        })
                        .orElseGet(() -> ArgumentParseResult.failure(new IllegalArgumentException("Could not find message in cache for input '" + inputQueue.peek() + "'.")));
                })
                .withSuggestionsProvider((context, input) -> {
                    final ServerPlayer target = target(context);
                    final MessageSignatureCache messageSignatureCache = messageSignatureCache(target);

                    return Arrays.stream(((MessageSignatureCacheAccessor) messageSignatureCache).access$entries())
                        .filter(Objects::nonNull)
                        .map(e -> UUID.nameUUIDFromBytes(e.bytes()))
                        .map(UUID::toString)
                        .toList();
                }))
            .permission("carbon.updateusername")
            .senderType(Commander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messageService.commandUpdateUsernameDescription().component())
            .handler(context -> {
                final ServerPlayer target = target(context);
                final MessageSignatureCache messageSignatureCache = messageSignatureCache(target);
                final MessageSignature messageSignature = context.get("message");
                target.connection.send(new ClientboundDeleteChatPacket(messageSignature.pack(messageSignatureCache)));
            })
            .build();

        this.commandManager.command(command);
    }

    private static MessageSignatureCache messageSignatureCache(final ServerPlayer target) {
        return ((ServerGamePacketListenerImplAccess) target.connection).accessor$messageSignatureCache();
    }

    private static ServerPlayer target(final CommandContext<Commander> context) {
        final SinglePlayerSelector selector = context.get("player");
        return selector.getSingle();
    }

}
