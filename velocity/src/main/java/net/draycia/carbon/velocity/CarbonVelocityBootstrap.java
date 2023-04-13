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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;

@Plugin(
    id = "$[ID]",
    name = "$[NAME]",
    version = "$[VERSION]",
    description = "$[DESCRIPTION]",
    url = "$[URL]",
    authors = {"Draycia"},
    dependencies = {
        @Dependency(id = "luckperms"),
        @Dependency(id = "miniplaceholders", optional = true)
    }
)
public class CarbonVelocityBootstrap {

    @Inject
    private Injector injector;
    @Inject
    private ProxyServer proxyServer;
    @Inject
    private PluginContainer pluginContainer;
    @Inject
    @DataDirectory
    private Path dataDirectory;
    private CarbonChatVelocity carbonVelocity;

    @Subscribe
    void onProxyInitialize(final ProxyInitializeEvent event) {
        this.carbonVelocity = this.injector.createChildInjector(new CarbonChatVelocityModule(
            this.pluginContainer, this.proxyServer, this.dataDirectory)).getInstance(CarbonChatVelocity.class);
        this.carbonVelocity.onInitialization(this);
    }

    @Subscribe
    void onProxyShutdown(final ProxyShutdownEvent event) {
        this.carbonVelocity.onShutdown();
    }

}
