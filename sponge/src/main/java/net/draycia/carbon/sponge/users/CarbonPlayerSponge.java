package net.draycia.carbon.sponge.users;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.locale.LocaleSource;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerSponge extends CarbonPlayerCommon {

    public CarbonPlayerSponge(
        final @NonNull String username,
        final @NonNull UUID uuid
    ) {
        super(username, uuid, Identity.identity(uuid));
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        super.displayName(displayName);

        this.player().ifPresent(player -> {
            if (displayName != null) {
                player.offer(Keys.CUSTOM_NAME, displayName);
            } else {
                player.remove(Keys.CUSTOM_NAME);
            }
        });
    }

    @Override
    public @NonNull Audience audience() {
        return this.player()
            .map(player -> (Audience) player)
            .orElseGet(Audience::empty);
    }

    private @NonNull Optional<ServerPlayer> player() {
        return Sponge.server().player(this.uuid);
    }

    @Override
    public @Nullable Locale locale() {
        return this.player()
            .map(LocaleSource::locale)
            .orElse(null);
    }

    @Override
    public @NonNull Component createItemHoverComponent() {
        final @Nullable ServerPlayer player = this.player().orElse(null);
        if (player == null) {
            return Component.empty();
        }

        final @Nullable ItemStack itemStack = player.equipped(EquipmentTypes.MAIN_HAND)
            .filter(it -> !it.isEmpty())
            .orElseGet(() -> player.equipped(EquipmentTypes.OFF_HAND).orElse(null));

        if (itemStack == null || itemStack.isEmpty()) {
            return Component.empty();
        }

        return this.fromStack(itemStack);
    }

    private @NonNull Component fromStack(final @NonNull ItemStack stack) {
        return stack.get(Keys.DISPLAY_NAME)

            // This is here as a fallback, but really, every ItemStack should
            // have a DISPLAY_NAME which is already formatted properly for us by the game.
            .orElseGet(() -> translatable()
                .key("chat.square_brackets")
                .args(stack.get(Keys.CUSTOM_NAME)
                    .map(name -> name.decorate(ITALIC))
                    .orElseGet(() -> stack.type().asComponent()))
                .hoverEvent(stack.createSnapshot())
                .apply(builder -> stack.get(Keys.ITEM_RARITY).ifPresent(rarity -> builder.color(rarity.color())))
                .build());
    }

    @Override
    public boolean hasPermission(final String permission) {
        final var player = this.player();

        // Ignore inspection. Don't make code harder to read, IntelliJ.
        if (player.isPresent()) {
            return player.get().hasPermission(permission);
        }

        return false;
    }

}
