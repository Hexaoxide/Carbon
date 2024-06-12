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
package net.draycia.carbon.velocity;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import javax.inject.Inject;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.config.MessagingSettings;
import net.draycia.carbon.common.util.CarbonDependencies;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.jpenilla.gremlin.runtime.platformsupport.VelocityClasspathAppender;

public final class CarbonVelocityBootstrap {

    private static final int BSTATS_PLUGIN_ID = 19505;

    private final PluginContainer pluginContainer;
    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;
    private final Inner inner;

    @Inject // use javax.inject, so it doesn't get relocated - we use Guice 7 and Velocity uses Guice 6.
    public CarbonVelocityBootstrap(
        final ProxyServer proxyServer,
        final PluginContainer pluginContainer,
        @DataDirectory final Path dataDirectory,
        final Metrics.Factory metricsFactory
    ) {
        this.proxy = proxyServer;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;

        this.inner = new Inner();
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        this.inner.onProxyInitialize(event);
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        this.inner.onProxyShutdown(event);
    }

    // Inner class to avoid classloading issues with guice
    private final class Inner {
        private @MonotonicNonNull Injector injector;

        void onProxyInitialize(final ProxyInitializeEvent event) {
            new VelocityClasspathAppender(CarbonVelocityBootstrap.this.proxy, CarbonVelocityBootstrap.this).append(
                CarbonDependencies.resolve(CarbonVelocityBootstrap.this.dataDirectory.resolve("libraries"))
            );

            this.injector = Guice.createInjector(
                new CarbonChatVelocityModule(
                    CarbonVelocityBootstrap.this,
                    CarbonVelocityBootstrap.this.pluginContainer,
                    CarbonVelocityBootstrap.this.proxy,
                    CarbonVelocityBootstrap.this.dataDirectory
                )
            );
            final Injector injector = this.injector;
            injector.getInstance(CarbonChatVelocity.class).onInitialization(CarbonVelocityBootstrap.this);

            final Metrics metrics = CarbonVelocityBootstrap.this.metricsFactory.make(CarbonVelocityBootstrap.this, BSTATS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("user_manager_type", () -> injector.getInstance(ConfigManager.class).primaryConfig().storageType().name()));
            metrics.addCustomChart(new SimplePie("messaging", () -> {
                final MessagingSettings settings = injector.getInstance(ConfigManager.class).primaryConfig().messagingSettings();
                if (!settings.enabled()) {
                    return "disabled";
                }
                return settings.brokerType().name();
            }));
        }

        void onProxyShutdown(final ProxyShutdownEvent event) {
            this.injector.getInstance(CarbonChatVelocity.class).onShutdown();
        }
    }

}
