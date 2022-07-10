package net.draycia.carbon.fabric.listeners;

import com.google.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.config.ConfigFactory;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class FabricChatPreviewListener implements ChatDecorator {

    private ConfigFactory configFactory;

    @Inject
    public FabricChatPreviewListener(final ConfigFactory configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public CompletableFuture<Component> decorate(@Nullable ServerPlayer serverPlayer, Component component) {
        String content = component.getString();

        for (final Map.Entry<String, String> placeholder : this.configFactory.primaryConfig().chatPlaceholders().entrySet()) {
            content = content.replace(placeholder.getKey(), placeholder.getValue());
        }

        final Component replaced = FabricAudiences.nonWrappingSerializer().serialize(MiniMessage.miniMessage().deserialize(content));
        return CompletableFuture.completedFuture(replaced);
    }

}
