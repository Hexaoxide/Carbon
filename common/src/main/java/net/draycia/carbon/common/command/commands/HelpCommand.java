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
package net.draycia.carbon.common.command.commands;

import com.google.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.suggestion.Suggestion;
import org.intellij.lang.annotations.Subst;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextColor.color;
import static org.incendo.cloud.minecraft.extras.MinecraftHelp.helpColors;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

@DefaultQualifier(NonNull.class)
public final class HelpCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final MinecraftHelp<Commander> minecraftHelp;

    @Inject
    public HelpCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageSource messageSource,
        final CarbonMessages carbonMessages
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.minecraftHelp = createHelp(commandManager, messageSource);
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("carbon");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "help");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .literal("help")
            .optional("query", greedyStringParser(), richDescription(this.carbonMessages.commandHelpArgumentQuery()), this::suggestQueries)
            .permission("carbon.help")
            .commandDescription(richDescription(this.carbonMessages.commandHelpDescription()))
            .handler(this::execute)
            .build();

        this.commandManager.command(command);
    }

    private void execute(final CommandContext<Commander> ctx) {
        this.minecraftHelp.queryCommands(ctx.getOrDefault("query", ""), ctx.sender());
    }

    private CompletableFuture<Iterable<Suggestion>> suggestQueries(final CommandContext<Commander> ctx, final CommandInput input) {
        final var result = this.commandManager.createHelpHandler().queryRootIndex(ctx.sender());
        return CompletableFuture.completedFuture(result.entries().stream().map(CommandEntry::syntax).map(Suggestion::suggestion).toList());
    }

    private static MinecraftHelp<Commander> createHelp(
        final CommandManager<Commander> manager,
        final CarbonMessageSource messageSource
    ) {
        return MinecraftHelp.<Commander>builder()
            .commandManager(manager)
            .audienceProvider(AudienceProvider.nativeAudience())
            .commandPrefix("/carbon help")
            .colors(helpColors(
                color(0xE099FF),
                WHITE,
                color(0xDD1BC4),
                GRAY,
                DARK_GRAY
            ))
            .messageProvider((sender, key, args) -> {
                final String messageKey = "command.help.misc." + key;
                final TagResolver.Builder tagResolver = TagResolver.builder();

                for (final Map.Entry<String, String> entry : args.entrySet()) {
                    @Subst("key") final String k = entry.getKey();
                    tagResolver.resolver(Placeholder.parsed(k, entry.getValue()));
                }

                return MiniMessage.miniMessage().deserialize(messageSource.messageOf(sender, messageKey), tagResolver.build());
            })
            .build();
    }

}
