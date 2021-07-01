package net.draycia.carbon.common;

import cloud.commandframework.CommandManager;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public abstract class CarbonChatCommon implements CarbonChat {

    public abstract CarbonMessageService messageService();

    private final CarbonEventHandler eventHandler = new CarbonEventHandler();

    public final void initialize() {
        final CommandManager<Commander> commandManager = this.createCommandManager();
        commandManager.command(commandManager.commandBuilder("carbon")
            .handler(ctx -> {
                final Commander sender = ctx.getSender();

                this.messageService().exampleCommandFeedback(sender, this.messageService().pluginName());

                if (sender instanceof PlayerCommander player) {
                    this.messageService().localeTestMessage(player.carbonPlayer());

                    final Component itemComponent = player.carbonPlayer().createItemHoverComponent();
                    if (itemComponent != empty()) {
                        player.sendMessage(TextComponent.ofChildren(text("Item: "), itemComponent));
                    } else {
                        player.sendMessage(text("You are not holding an item!"));
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
