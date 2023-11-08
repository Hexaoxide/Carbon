package net.draycia.carbon.common;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import net.draycia.carbon.common.integration.Integration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class CarbonPlatformModule extends AbstractModule {

    @Override
    protected void configure() {
        this.configureIntegrations(
            Multibinder.newSetBinder(this.binder(), Integration.class),
            Multibinder.newSetBinder(this.binder(), Integration.ConfigMeta.class)
        );
    }

    protected void configureIntegrations(
        final Multibinder<Integration> integrations,
        final Multibinder<Integration.ConfigMeta> configs
    ) {
    }


}
