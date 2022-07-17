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
    public CompletableFuture<Component> decorate(final @Nullable ServerPlayer serverPlayer, final Component component) {
        String content = component.getString();

        for (final Map.Entry<String, String> placeholder : this.configFactory.primaryConfig().chatPlaceholders().entrySet()) {
            content = content.replace(placeholder.getKey(), placeholder.getValue());
        }

        final Component replaced = FabricAudiences.nonWrappingSerializer().serialize(MiniMessage.miniMessage().deserialize(content));
        return CompletableFuture.completedFuture(replaced);
    }

}
