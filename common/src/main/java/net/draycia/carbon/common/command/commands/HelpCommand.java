/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import com.google.inject.Inject;
import java.util.List;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextColor.color;

@DefaultQualifier(NonNull.class)
public final class HelpCommand {

    private final CommandManager<Commander> manager;
    private final MinecraftHelp<Commander> help;

    @Inject
    public HelpCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageSource messageSource
    ) {
        this.manager = commandManager;
        this.help = createHelp(commandManager, messageSource);

        final var command = commandManager.commandBuilder("carbon")
            .literal("help")
            .argument(StringArgument.<Commander>newBuilder("query")
                .greedy()
                .withSuggestionsProvider(this::suggestQueries)
                .asOptional())
            .permission("carbon.help")
            .handler(this::execute)
            .build();

        commandManager.command(command);
    }

    private void execute(final CommandContext<Commander> ctx) {
        this.help.queryCommands(ctx.getOrDefault("query", ""), ctx.getSender());
    }

    private List<String> suggestQueries(final CommandContext<Commander> ctx, final String input) {
        final var topic = (CommandHelpHandler.IndexHelpTopic<Commander>) this.manager.getCommandHelpHandler().queryHelp(ctx.getSender(), "");
        return topic.getEntries().stream().map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString).toList();
    }

    private static MinecraftHelp<Commander> createHelp(
        final CommandManager<Commander> manager,
        final CarbonMessageSource messageSource
    ) {
        final MinecraftHelp<Commander> help = new MinecraftHelp<>(
            "/carbon help",
            AudienceProvider.nativeAudience(),
            manager
        );

        help.setHelpColors(
            MinecraftHelp.HelpColors.of(
                color(0xE099FF),
                WHITE,
                color(0xDD1BC4),
                GRAY,
                DARK_GRAY
            )
        );

        help.messageProvider((sender, key, args) -> {
            final String messageKey = "command.help." + key;
            final TemplateResolver resolver;

            // Total hack but works for now
            if (args.length == 2) {
                resolver = TemplateResolver.templates(
                    Template.template("page", text(args[0])),
                    Template.template("max_pages", text(args[1]))
                );
            } else {
                resolver = TemplateResolver.empty();
            }

            return MiniMessage.miniMessage().deserialize(messageSource.messageOf(sender, messageKey), resolver);
        });

        return help;
    }

}
