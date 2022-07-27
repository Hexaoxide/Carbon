package net.draycia.carbon.fabric.mixin;

import java.util.function.Predicate;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {

    @Invoker
    void callBroadcastChatMessage(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer serverPlayer, ChatSender chatSender, ChatType.Bound bound);

}
