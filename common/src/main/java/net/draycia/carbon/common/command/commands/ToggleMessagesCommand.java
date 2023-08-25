package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;

public class ToggleMessagesCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final ArgumentFactory argumentFactory;

    @Inject
    public ToggleMessagesCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ArgumentFactory argumentFactory
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.argumentFactory = argumentFactory;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("togglemsg", "togglepm");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "togglemsg");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.togglemsg")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandToggleMsgDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final boolean nowIgnoring = !sender.ignoringDirectMessages();
                sender.ignoringDirectMessages(nowIgnoring);

                if (nowIgnoring) {
                    this.carbonMessages.whispersToggledOff(sender);
                } else {
                    this.carbonMessages.whispersToggledOn(sender);
                }
            })
            .build();

        this.commandManager.command(command);

        final var toggleOn = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.togglemsg")
            .literal("on", "allow")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandToggleMsgDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                sender.ignoringDirectMessages(false);
                this.carbonMessages.whispersToggledOn(sender);
            })
            .build();

        this.commandManager.command(toggleOn);

        final var toggleOff = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.togglemsg")
            .literal("off", "ignore")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandToggleMsgDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                sender.ignoringDirectMessages(true);
                this.carbonMessages.whispersToggledOff(sender);
            })
            .build();

        this.commandManager.command(toggleOff);
    }

}
