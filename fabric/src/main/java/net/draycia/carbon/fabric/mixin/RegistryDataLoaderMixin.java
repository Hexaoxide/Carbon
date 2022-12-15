package net.draycia.carbon.fabric.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RegistryDataLoader.class)
abstract class RegistryDataLoaderMixin {
    @Inject(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    @SuppressWarnings("unchecked")
    private static void carbon$injectMessageTypes(
        final ResourceManager resourceManager,
        final RegistryAccess registryAccess,
        final List<RegistryDataLoader.RegistryData<?>> data,
        final CallbackInfoReturnable<RegistryAccess.Frozen> cir,
        final Map map,
        final List<Pair<WritableRegistry<?>, ?>> list
    ) {
        for (final Pair<WritableRegistry<?>, ?> pair : list) {
            final WritableRegistry<?> registryKey = pair.getFirst();
            if (registryKey.key() == Registries.CHAT_TYPE) {
                Registry.register((Registry<ChatType>) pair.getFirst(), CarbonChatFabric.CHAT_TYPE, carbonChatType());
            }
        }
    }

    private static ChatType carbonChatType() {
        return new ChatType(ChatTypeDecoration.withSender("%s"), ChatTypeDecoration.withSender("%s"));
    }
}
