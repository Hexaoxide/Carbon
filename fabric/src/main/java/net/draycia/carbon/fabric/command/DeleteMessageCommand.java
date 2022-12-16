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
import cloud.commandframework.types.tuples.Pair;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
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
import net.minecraft.server.players.PlayerList;
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
            .permission("carbon.deletemessage");

        // todo: might be better to have one command with a MultiplePlayerSelector argument?
        final var forPlayer = root
            .argument(SinglePlayerSelectorArgument.of("recipient"))
            .argument(this.commandManager.argumentBuilder(MessageSignature.class, "message")
                .withParser((context, inputQueue) -> result(inputQueue, pairs(target(context))))
                .withSuggestionsProvider((context, input) -> messageIdStrings(target(context)).toList()))
            .handler(context -> {
                final ServerPlayer target = target(context);
                final MessageSignatureCache messageSignatureCache = messageSignatureCache(target);
                final MessageSignature messageSignature = context.get("message");
                target.connection.send(new ClientboundDeleteChatPacket(messageSignature.pack(messageSignatureCache)));
            })
            .build();
        this.commandManager.command(forPlayer);

        final var global = root.literal("all")
            .argument(this.commandManager.argumentBuilder(MessageSignature.class, "message")
                .withParser((context, inputQueue) -> result(inputQueue, allPairs(((FabricCommander) context.getSender()).commandSourceStack().getServer().getPlayerList())))
                .withSuggestionsProvider((context, input) -> allMessageIdStrings(((FabricCommander) context.getSender()).commandSourceStack().getServer().getPlayerList())))
            .handler(context -> {
                final PlayerList playerList = ((FabricCommander) context.getSender()).commandSourceStack().getServer().getPlayerList();
                final MessageSignature messageSignature = context.get("message");
                for (final ServerPlayer player : playerList.getPlayers()) {
                    final MessageSignatureCache messageSignatureCache = messageSignatureCache(player);
                    player.connection.send(new ClientboundDeleteChatPacket(messageSignature.pack(messageSignatureCache)));
                }
            })
            .build();
        this.commandManager.command(global);
    }

    private static ArgumentParseResult<MessageSignature> result(final Queue<String> inputQueue, final Stream<Pair<MessageSignature, UUID>> stream) {
        return stream.findFirst()
            .filter(s -> s.getSecond().toString().equals(inputQueue.peek()))
            .map(pair -> {
                inputQueue.poll();
                return ArgumentParseResult.success(pair.getFirst());
            })
            .orElseGet(() -> ArgumentParseResult.failure(new IllegalArgumentException("Could not find message in cache for input '" + inputQueue.peek() + "'.")));
    }

    private static Stream<Pair<MessageSignature, UUID>> allPairs(final PlayerList playerList) {
        return playerList.getPlayers().stream().flatMap(DeleteMessageCommand::pairs).distinct();
    }

    private static Stream<Pair<MessageSignature, UUID>> pairs(final ServerPlayer player) {
        return Arrays.stream(((MessageSignatureCacheAccessor) messageSignatureCache(player)).access$entries())
            .filter(Objects::nonNull)
            .map(e -> Pair.of(e, UUID.nameUUIDFromBytes(e.bytes())));
    }

    private static List<String> allMessageIdStrings(final PlayerList playerList) {
        return playerList.getPlayers().stream()
            .flatMap(DeleteMessageCommand::messageIdStrings)
            .distinct()
            .toList();
    }

    private static Stream<String> messageIdStrings(final ServerPlayer player) {
        return Arrays.stream(((MessageSignatureCacheAccessor) messageSignatureCache(player)).access$entries())
            .filter(Objects::nonNull)
            .map(e -> UUID.nameUUIDFromBytes(e.bytes()))
            .map(UUID::toString);
    }

    private static MessageSignatureCache messageSignatureCache(final ServerPlayer target) {
        return ((ServerGamePacketListenerImplAccess) target.connection).accessor$messageSignatureCache();
    }

    private static ServerPlayer target(final CommandContext<Commander> context) {
        final SinglePlayerSelector selector = context.get("recipient");
        return selector.getSingle();
    }

}
