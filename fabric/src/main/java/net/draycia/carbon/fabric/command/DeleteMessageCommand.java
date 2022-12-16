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
import cloud.commandframework.fabric.argument.server.MultiplePlayerSelectorArgument;
import cloud.commandframework.fabric.data.MultiplePlayerSelector;
import cloud.commandframework.types.tuples.Pair;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Stream;
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
        final var root = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.deletemessage")
            .argument(MultiplePlayerSelectorArgument.of("recipients"));

        final var specificMessage = root
            .argument(this.commandManager.argumentBuilder(MessageSignature.class, "message")
                .withParser((context, inputQueue) -> result(inputQueue, context.<MultiplePlayerSelector>get("recipients").get().stream().flatMap(DeleteMessageCommand::pairs).distinct()))
                .withSuggestionsProvider((context, input) -> context.<MultiplePlayerSelector>get("recipients").get().stream().flatMap(DeleteMessageCommand::messageIdStrings).distinct().toList()))
            .handler(context -> {
                final MessageSignature messageSignature = context.get("message");
                for (final ServerPlayer player : context.<MultiplePlayerSelector>get("recipients").get()) {
                    deleteMessage(player, messageSignature);
                }
            })
            .build();
        this.commandManager.command(specificMessage);

        final var allMessages = root
            .literal("all")
            .handler(context -> {
                for (final ServerPlayer player : context.<MultiplePlayerSelector>get("recipients").get()) {
                    entryStream(messageSignatureCache(player))
                        .forEach(messageSignature -> deleteMessage(player, messageSignature));
                }
            })
            .build();
        this.commandManager.command(allMessages);
    }

    private static void deleteMessage(final ServerPlayer player, final MessageSignature messageSignature) {
        final MessageSignatureCache messageSignatureCache = messageSignatureCache(player);
        player.connection.send(new ClientboundDeleteChatPacket(messageSignature.pack(messageSignatureCache)));
    }

    private static ArgumentParseResult<MessageSignature> result(final Queue<String> inputQueue, final Stream<Pair<MessageSignature, UUID>> stream) {
        return stream.filter(s -> s.getSecond().toString().equals(inputQueue.peek()))
            .findFirst()
            .map(pair -> {
                inputQueue.poll();
                return ArgumentParseResult.success(pair.getFirst());
            })
            .orElseGet(() -> ArgumentParseResult.failure(new IllegalArgumentException("Could not find message in cache for input '" + inputQueue.peek() + "'.")));
    }

    private static Stream<Pair<MessageSignature, UUID>> pairs(final ServerPlayer player) {
        return entryStream(messageSignatureCache(player)).map(messageSignature -> Pair.of(messageSignature, uuid(messageSignature)));
    }

    private static Stream<String> messageIdStrings(final ServerPlayer player) {
        return entryStream(messageSignatureCache(player))
            .map(DeleteMessageCommand::uuid)
            .map(UUID::toString);
    }

    private static UUID uuid(final MessageSignature messageSignature) {
        return UUID.nameUUIDFromBytes(messageSignature.bytes());
    }

    private static Stream<MessageSignature> entryStream(final MessageSignatureCache cache) {
        return Arrays.stream(((MessageSignatureCacheAccessor) cache).access$entries()).filter(Objects::nonNull);
    }

    private static MessageSignatureCache messageSignatureCache(final ServerPlayer target) {
        return ((ServerGamePacketListenerImplAccess) target.connection).accessor$messageSignatureCache();
    }

}
