package net.draycia.carbon.fabric.mixin;

import net.draycia.carbon.fabric.CarbonChatFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatType.class)
public abstract class ChatTypeMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(Registry<ChatType> registry, CallbackInfoReturnable<RegistryAccess.RegistryEntry<ChatType>> cir) {
        BuiltinRegistries.register(registry, CarbonChatFabric.CHAT_TYPE,
            new ChatType(ChatTypeDecoration.withSender("%s"), ChatTypeDecoration.withSender("%s")));
    }

}
