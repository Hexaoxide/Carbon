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
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.config.MessagingSettings;
import net.draycia.carbon.common.util.CarbonDependencies;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public final class CarbonVelocityBootstrap {

    private static final int BSTATS_PLUGIN_ID = 19505;

    private final Injector parentInjector;
    private final PluginContainer pluginContainer;
    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;
    private @MonotonicNonNull Injector injector;

    @Inject
    public CarbonVelocityBootstrap(
        final Injector injector,
        final ProxyServer proxyServer,
        final PluginContainer pluginContainer,
        @DataDirectory final Path dataDirectory,
        final Metrics.Factory metricsFactory
    ) {
        this.proxy = proxyServer;
        this.parentInjector = injector;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        CarbonDependencies.load(
            this.dataDirectory.resolve("libraries"),
            path -> this.proxy.getPluginManager().addToClasspath(this, path)
        );

        this.injector = this.parentInjector.createChildInjector(
            new CarbonChatVelocityModule(this.pluginContainer, this.proxy, this.dataDirectory));
        this.injector.getInstance(CarbonChatVelocity.class).onInitialization(this);

        final Metrics metrics = this.metricsFactory.make(this, BSTATS_PLUGIN_ID);
        metrics.addCustomChart(new SimplePie("user_manager_type", () -> this.injector.getInstance(ConfigManager.class).primaryConfig().storageType().name()));
        metrics.addCustomChart(new SimplePie("messaging", () -> {
            final MessagingSettings settings = this.injector.getInstance(ConfigManager.class).primaryConfig().messagingSettings();
            if (!settings.enabled()) {
                return "disabled";
            }
            return settings.brokerType().name();
        }));
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        this.injector.getInstance(CarbonChatVelocity.class).onShutdown();
    }

}
