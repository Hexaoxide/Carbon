package net.draycia.carbon.common.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface UserManagerInternal<C extends CarbonPlayer> extends UserManager<C> {

    void shutdown();

    CompletableFuture<Void> save(C player);

    CompletableFuture<Void> loggedOut(UUID uuid);

    @Override
    default CompletableFuture<ComponentPlayerResult<C>> savePlayer(final C player) {
        return this.save(player)
            .thenApply($ -> new ComponentPlayerResult<>(player, Component.empty()))
            .exceptionally(thr -> new ComponentPlayerResult<>(null, ComponentMessageThrowable.getOrConvertMessage(thr)));
    }

}
