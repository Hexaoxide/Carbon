/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.paper;

import com.google.inject.Guice;
import net.draycia.carbon.api.CarbonChatProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonPaperBootstrap extends JavaPlugin {

    private @MonotonicNonNull CarbonChatPaper carbonChat;

    @Override
    public void onLoad() {
        this.carbonChat = Guice.createInjector(new CarbonChatPaperModule(this))
            .getInstance(CarbonChatPaper.class);
        CarbonChatProvider.register(this.carbonChat);
    }

    @Override
    public void onEnable() {
        if (this.carbonChat != null) {
            this.carbonChat.onEnable();
        }
    }

    @Override
    public void onDisable() {
        if (this.carbonChat != null) {
            this.carbonChat.onDisable();
        }
    }

}
