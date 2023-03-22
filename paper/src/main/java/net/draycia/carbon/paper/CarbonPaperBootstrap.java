/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
import com.google.inject.Injector;
import io.papermc.lib.PaperLib;
import java.util.logging.Level;
import net.draycia.carbon.api.CarbonChatProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonPaperBootstrap extends JavaPlugin {

    private @MonotonicNonNull CarbonChatPaper carbonChat;
    private @MonotonicNonNull UnsupportedPlatformException unsupportedPlatform;

    @Override
    public void onLoad() {
        this.unsupportedPlatform = this.checkPlatform();
        if (this.unsupportedPlatform != null) {
            this.getLogger().log(Level.SEVERE, this.unsupportedPlatform.getMessage(), this.unsupportedPlatform);
            return;
        }

        this.carbonChat = new CarbonChatPaper();
        final Injector injector = Guice.createInjector(new CarbonChatPaperModule(this, this.carbonChat));
        CarbonChatProvider.register(this.carbonChat);
        injector.injectMembers(this.carbonChat);
    }

    // null = success
    private @Nullable UnsupportedPlatformException checkPlatform() {
        if (PaperLib.isPaper()) {
            return this.checkPaperVersion();
        }
        this.severe("*");
        this.severe("* CarbonChat makes extensive use of APIs added by Paper.");
        this.severe("* For this reason, CarbonChat requires Paper and is not");
        this.severe("* compatible with Spigot or CraftBukkit servers.");
        this.severe("* Upgrade your userManager to Paper in order to use CarbonChat.");
        this.severe("*");
        PaperLib.suggestPaper(this, Level.SEVERE);
        return new UnsupportedPlatformException("Not Paper or a Paper-based userManager runtime");
    }

    private @Nullable UnsupportedPlatformException checkPaperVersion() {
        // assume major version 1
        final int minor = PaperLib.getMinecraftVersion();
        final int patch = PaperLib.getMinecraftPatchVersion();
        if (minor != 19 || patch < 3) {
            return new UnsupportedPlatformException("Wrong Minecraft version (" + Bukkit.getMinecraftVersion() + "), this build of Carbon is for 1.19.3 and newer");
        }
        return null;
    }

    private void severe(final String message) {
        this.getLogger().log(Level.SEVERE, message);
    }

    @Override
    public void onEnable() {
        if (this.unsupportedPlatform != null) {
            this.getLogger().log(Level.SEVERE, this.unsupportedPlatform.getMessage(), this.unsupportedPlatform);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
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

    private static final class UnsupportedPlatformException extends RuntimeException {
        UnsupportedPlatformException(final String reason) {
            super("Your userManager does not support this build of Carbon: " + reason);
        }
    }

}
