package net.draycia.carbonmoderation.commands;

import net.draycia.carbon.libs.co.aikar.commands.BaseCommand;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandAlias;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandPermission;
import net.draycia.carbon.libs.co.aikar.commands.annotation.Default;
import net.draycia.carbon.libs.co.aikar.commands.annotation.Dependency;
import net.draycia.carbon.libs.net.kyori.adventure.text.Component;
import net.draycia.carbonmoderation.CarbonChatModeration;
import org.bukkit.command.CommandSender;

@CommandAlias("modreload|mreload")
@CommandPermission("carbonchat.reload")
public class ModReloadCommand extends BaseCommand {

    private final CarbonChatModeration moderation;

    public ModReloadCommand(CarbonChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    public void baseCommand(CommandSender sender) {
        moderation.reloadConfig();

        Component message = moderation.getCarbonChat().getAdventureManager()
                .processMessage(moderation.getConfig().getString("language.reloaded"),
                "br", "\n");

        moderation.getCarbonChat().getAdventureManager().getAudiences().audience(sender).sendMessage(message);
    }

}
