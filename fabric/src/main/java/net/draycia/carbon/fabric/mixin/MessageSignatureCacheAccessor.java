package net.draycia.carbon.fabric.mixin;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MessageSignatureCache.class)
public interface MessageSignatureCacheAccessor {

    @Accessor("entries")
    @Final
    MessageSignature[] access$entries();

}
