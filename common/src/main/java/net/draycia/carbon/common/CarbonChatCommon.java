package net.draycia.carbon.common;

import cloud.commandframework.CommandManager;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public abstract class CarbonChatCommon implements CarbonChat {

    private final CarbonEventHandler eventHandler = new CarbonEventHandler();

    public final void initialize() {
        final CommandManager<Commander> commandManager = this.createCommandManager();
        commandManager.command(commandManager.commandBuilder("carbon")
            .handler(ctx -> {
                final Commander sender = ctx.getSender();
                sender.sendMessage(text()
                    .content("Hello from ")
                    .append(text("Carbon", DARK_GRAY, BOLD))
                    .append(text('!')));
                if (sender instanceof PlayerCommander) {
                    final Component itemComponent = ((PlayerCommander) sender).carbonPlayer().createItemHoverComponent();
                    if (itemComponent != empty()) {
                        sender.sendMessage(TextComponent.ofChildren(text("Item: "), itemComponent));
                    } else {
                        sender.sendMessage(text("You are not holding an item!"));
                    }
                }
            }));
    }

    protected abstract @NonNull CommandManager<Commander> createCommandManager();

    @Override
    public final @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

}
