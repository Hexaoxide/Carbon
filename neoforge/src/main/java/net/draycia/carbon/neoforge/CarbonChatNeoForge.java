package net.draycia.carbon.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("carbonchat")
public final class CarbonChatNeoForge {
    public CarbonChatNeoForge(final IEventBus modBus) {
        System.out.println("Hello, carbonchat!");
    }
}
