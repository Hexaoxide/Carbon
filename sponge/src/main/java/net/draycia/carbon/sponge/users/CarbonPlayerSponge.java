package net.draycia.carbon.sponge.users;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.locale.LocaleSource;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerSponge implements CarbonPlayer, ForwardingAudience.Single {

    private final CarbonPlayer carbonPlayer;

    public CarbonPlayerSponge(final CarbonPlayer carbonPlayer) {
        this.carbonPlayer = carbonPlayer;
    }

    public CarbonPlayer carbonPlayer() {
        return this.carbonPlayer;
    }

    @Override
    public @NotNull Audience audience() {
        return this.player()
            .map(player -> (Audience) player)
            .orElseGet(Audience::empty);
    }

    @Override
    public String username() {
        return this.carbonPlayer.username();
    }

    @Override
    public boolean hasCustomDisplayName() {
        return this.carbonPlayer.hasCustomDisplayName();
    }

    @Override
    public @Nullable Component displayName() {
        return this.carbonPlayer.displayName();
    }

    @Override
    public void displayName(final @Nullable Component displayName) {
        this.carbonPlayer.displayName(displayName);

        this.player().ifPresent(player -> {
            if (displayName != null) {
                player.offer(Keys.CUSTOM_NAME, displayName);
            } else {
                player.remove(Keys.CUSTOM_NAME);
            }
        });
    }

    @Override
    public UUID uuid() {
        return this.carbonPlayer.uuid();
    }

    private Optional<ServerPlayer> player() {
        return Sponge.server().player(this.carbonPlayer.uuid());
    }

    @Override
    public Component createItemHoverComponent() {
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

    private Component fromStack(final ItemStack stack) {
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
        return player.map(serverPlayer -> serverPlayer.hasPermission(permission))
            .orElse(false);
    }

    @Override
    public String primaryGroup() {
        return "default"; // TODO: implement
    }

    @Override
    public List<String> groups() {
        return List.of("default"); // TODO: implement
    }

    @Override
    public boolean globallyMuted() {
        return this.carbonPlayer.globallyMuted();
    }

    @Override
    public void globallyMuted(final boolean muted) {
        this.carbonPlayer.globallyMuted(muted);
    }

    @Override
    public boolean deafened() {
        return this.carbonPlayer.deafened();
    }

    @Override
    public void deafened(final boolean deafened) {
        this.carbonPlayer.deafened(deafened);
    }

    @Override
    public boolean spying() {
        return this.carbonPlayer.spying();
    }

    @Override
    public void spying(final boolean spying) {
        this.carbonPlayer.spying(spying);
    }

    @Override
    public void sendMessageAsPlayer(final String message) {
        this.player().ifPresent(player -> player.simulateChat(Component.text(message), Cause.builder().build()));
    }

    @Override
    public boolean online() {
        final var player = this.player();
        return player.isPresent() && player.get().isOnline();
    }

    @Override
    public @Nullable UUID whisperReplyTarget() {
        return this.carbonPlayer.whisperReplyTarget();
    }

    @Override
    public void whisperReplyTarget(final @Nullable UUID uuid) {
        this.carbonPlayer.whisperReplyTarget(uuid);
    }

    @Override
    public @Nullable UUID lastWhisperTarget() {
        return this.carbonPlayer.lastWhisperTarget();
    }

    @Override
    public void lastWhisperTarget(final @Nullable UUID uuid) {
        this.carbonPlayer.lastWhisperTarget(uuid);
    }

    @Override
    public @Nullable Locale locale() {
        return this.player()
            .map(LocaleSource::locale)
            .orElse(null);
    }

    @Override
    public @Nullable ChatChannel selectedChannel() {
        return this.carbonPlayer.selectedChannel();
    }

    @Override
    public void selectedChannel(final ChatChannel chatChannel) {
        this.carbonPlayer.selectedChannel(chatChannel);
    }

    @Override
    public @NotNull Identity identity() {
        return this.carbonPlayer.identity();
    }

}
