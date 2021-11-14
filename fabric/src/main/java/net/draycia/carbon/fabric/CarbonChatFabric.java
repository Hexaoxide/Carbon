package net.draycia.carbon.fabric;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ListenerUtils;
import net.draycia.carbon.fabric.callback.FabricChatCallback;
import net.draycia.carbon.fabric.listeners.FabricChatListener;
import net.fabricmc.api.ModInitializer;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatFabric implements ModInitializer, CarbonChat {

    private final CarbonEventHandler eventHandler = new CarbonEventHandler();
    private @MonotonicNonNull Injector injector;
    private @MonotonicNonNull UserManager<CarbonPlayerCommon> userManager;
    private @MonotonicNonNull Logger logger;
    private @MonotonicNonNull CarbonServerFabric carbonServerFabric;
    private @MonotonicNonNull CarbonMessageService messageService;
    private @MonotonicNonNull ChannelRegistry channelRegistry;

    @Override
    public void onInitialize() {
        CarbonChatProvider.register(this);

        this.injector = Guice.createInjector(new CarbonChatFabricModule(this, this.dataDirectory()));
        this.logger = LogManager.getLogger("CarbonChat");
        this.messageService = this.injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServerFabric = this.injector.getInstance(CarbonServerFabric.class);
        this.userManager = this.injector.getInstance(com.google.inject.Key.get(new TypeLiteral<UserManager<CarbonPlayerCommon>>() {}));

        // Platform Listeners
        FabricChatCallback.setup();
        FabricChatCallback.INSTANCE.registerListener(new FabricChatListener());

        // Listeners
        ListenerUtils.registerCommonListeners(this.injector);

        // Commands
        CloudUtils.registerCommands(this.injector);

        // TODO: save player data, find scheduler or use java's

        // Load channels
        ((CarbonChannelRegistry) this.channelRegistry()).loadChannels();

        // TODO: save player data on shutdown
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

    @Override
    public CarbonServer server() {
        return this.carbonServerFabric;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    @Override
    public IMessageRenderer<SourcedAudience, String, RenderedMessage, Component> messageRenderer() {
        return this.injector.getInstance(FabricMessageRenderer.class);
    }

    public MinecraftServer minecraftServer() {

    }

    public CarbonMessageService messageService() {
        return this.messageService;
    }

}
