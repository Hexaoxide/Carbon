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

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import cloud.commandframework.permission.Permission;
import com.google.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.command.argument.OptionValueParser;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.Nullable;

@DefaultQualifier(NonNull.class)
public class NicknameCommand extends CarbonCommand {

    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;
    final PlayerSuggestions playerSuggestions;

    @Inject
    public NicknameCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final PlayerSuggestions playerSuggestions
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.playerSuggestions = playerSuggestions;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("nickname", "nick");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "nickname");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            // TODO: Allow UUID input for target player
            .flag(this.commandManager.flagBuilder("player")
                .withAliases("p")
                .withDescription(RichDescription.of(this.carbonMessages.commandNicknameArgumentPlayer().component()))
                .withArgument(CarbonPlayerArgument.newBuilder("player").withMessages(this.carbonMessages).withSuggestionsProvider(this.playerSuggestions).asOptional())
                .withPermission(Permission.of("carbon.nickname.others"))
            )
            .flag(this.commandManager.flagBuilder("nickname")
                .withAliases("n")
                .withDescription(RichDescription.of(this.carbonMessages.commandNicknameArgumentNickname().component()))
                .withArgument(FlagArgument.<Commander, String>ofType(String.class, "value")
                    .withParser(new OptionValueParser<>())
                    .asOptional()
                    .build())
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .flag(this.commandManager.flagBuilder("reset")
                .withAliases("r")
                .withDescription(RichDescription.of(this.carbonMessages.commandNicknameArgumentReset().component()))
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .permission("carbon.nickname")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandNicknameDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (handler.flags().contains("reset")) {
                    final CarbonPlayer target = handler.flags().contains("player") ?
                        handler.flags().get("player") : sender;

                    target.displayName(null);

                    this.carbonMessages.nicknameReset(target);

                    if (sender != target) {
                        this.carbonMessages.nicknameResetOthers(sender, target.username());
                    }

                    return;
                }

                // Setting nickname
                if (handler.flags().contains("nickname")) {
                    // Lazy since the player might not have permission to set the nickname
                    final var ref = new Object() {
                        @Nullable Component cached = null;
                    };
                    final Supplier<Component> lazyNickname = () -> {
                        if (ref.cached != null) return ref.cached;

                        final var builder = MiniMessage.builder()
                            .tags(this.resolver(sender))
                            .build();
                        ref.cached = builder.deserialize(handler.flags().get("nickname"));

                        return ref.cached;
                    };

                    final @MonotonicNonNull CarbonPlayer target = handler.flags().get("player");

                    // Setting other player's nickname
                    if (target != null && !target.equals(sender)) {
                        this.carbonMessages.nicknameSet(target, lazyNickname.get());
                        this.carbonMessages.nicknameSetOthers(sender, target.username(), lazyNickname.get());
                    } else {
                        // Setting own nickname
                        if (!sender.hasPermission("carbon.nickname.self")) {
                            this.carbonMessages.nicknameCannotSetOwn(sender);
                            return;
                        }

                        sender.displayName(lazyNickname.get());
                        this.carbonMessages.nicknameSet(sender, lazyNickname.get());
                    }
                } else if (handler.flags().contains("player")) {
                    // Checking other player's nickname
                    final CarbonPlayer target = handler.flags().get("player");

                    if (target.displayName() != null) {
                        this.carbonMessages.nicknameShowOthers(sender, target.username(), target.displayName());
                    } else {
                        this.carbonMessages.nicknameShowOthersUnset(sender, target.username());
                    }
                } else {
                    // Checking own nickname
                    if (!sender.hasPermission("carbon.nickname.self")) {
                        this.carbonMessages.nicknameCannotSeeOwn(sender);
                        return;
                    }

                    if (sender.displayName() != null) {
                        this.carbonMessages.nicknameShow(sender, sender.username(), sender.displayName());
                    } else {
                        this.carbonMessages.nicknameShowUnset(sender, sender.username());
                    }
                }
            })
            .build();

        this.commandManager.command(command);
    }

    private final String[] styleStrings = {"color", "gradient", "decorations", "hoverEvent", "clickEvent", "insertion", "rainbow", "reset"};

    private TagResolver resolver(final CarbonPlayer carbonPlayer) {
        if (carbonPlayer.hasPermission("carbon.nickname.style.*")) {
            return TagResolver.standard();
        }

        final var resolver = TagResolver.builder();
        for (final String style : this.styleStrings) {
            if (carbonPlayer.hasPermission("carbon.nickname.style.%s".formatted(style))) {
                try {
                    final var method = StandardTags.class.getDeclaredMethod(style);
                    final var tag = (TagResolver) method.invoke(null);
                    resolver.resolvers(tag);
                } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) { }
            }
        }

        return resolver.build();
    }

}
