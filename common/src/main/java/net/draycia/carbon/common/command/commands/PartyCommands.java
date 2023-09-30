/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.types.tuples.Pair;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import java.util.Map;
import java.util.UUID;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.users.PartyImpl;
import net.draycia.carbon.common.users.PartyInvites;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

// TODO auto create party channel/config and add toggle for party feature
@DefaultQualifier(NonNull.class)
public final class PartyCommands extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final ArgumentFactory argumentFactory;
    private final UserManagerInternal<?> userManager;
    private final PartyInvites partyInvites;
    private final ConfigManager config;

    @Inject
    public PartyCommands(
        final CommandManager<Commander> commandManager,
        final ArgumentFactory argumentFactory,
        final UserManagerInternal<?> userManager,
        final PartyInvites partyInvites,
        final ConfigManager config
    ) {
        this.commandManager = commandManager;
        this.argumentFactory = argumentFactory;
        this.userManager = userManager;
        this.partyInvites = partyInvites;
        this.config = config;
    }

    @Override
    public void init() {
        if (!this.config.primaryConfig().partyChat()) {
            return;
        }

        final var root = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases());
        this.commandManager.command(root.handler(this::info));
        this.commandManager.command(
            root.literal("create")
                .argument(StringArgument.<Commander>builder("name").greedy().asOptional())
                .senderType(PlayerCommander.class)
                .handler(this::createParty)
        );
        this.commandManager.command(
            root.literal("invite")
                .senderType(PlayerCommander.class)
                .argument(this.argumentFactory.carbonPlayer("player"))
                .handler(this::invitePlayer)
        );
        this.commandManager.command(
            root.literal("accept")
                .senderType(PlayerCommander.class)
                .argument(this.argumentFactory.carbonPlayer("sender").asOptional())
                .handler(this::acceptInvite)
        );
        this.commandManager.command(
            root.literal("leave")
                .senderType(PlayerCommander.class)
                .handler(this::leaveParty)
        );
        this.commandManager.command(
            root.literal("disband")
                .senderType(PlayerCommander.class)
                .handler(this::disbandParty)
        );
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("party", "group");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "party");
    }

    private void info(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable UUID pid = player.party();
        final @Nullable PartyImpl party = pid != null ? this.userManager.party(pid).join() : null;
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED));
        } else {
            player.sendMessage(Component.text("You are in the party '" + party.name() + "'"));
        }
    }

    private void createParty(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable UUID oldId = player.party();
        final @Nullable PartyImpl oldParty = oldId != null ? this.userManager.party(oldId).join() : null;
        if (oldParty != null) {
            player.sendMessage(Component.text("You must leave your current party first.", NamedTextColor.RED));
            return;
        }
        final String name = ctx.getOrDefault("name", player.username() + "'s party");
        if (name.toCharArray().length > 256) {
            player.sendMessage(Component.text("Party name is too long.", NamedTextColor.RED));
            return;
        }
        final PartyImpl party = PartyImpl.create(name, this.userManager);
        party.addMember(player.uuid());
    }

    private void invitePlayer(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final CarbonPlayer recipient = ctx.get("player");
        final @Nullable UUID partyId = player.party();
        if (partyId == null) {
            mustBeInParty(player);
            return;
        }
        final @Nullable PartyImpl party = this.userManager.party(partyId).join();
        if (party == null) {
            mustBeInParty(player);
            return;
        }
        this.partyInvites.sendInvite(player.uuid(), recipient.uuid(), party.id());
        recipient.sendMessage(Component.text("u got invited to " + party.name()));
    }

    private void acceptInvite(final CommandContext<Commander> ctx) {
        final @Nullable CarbonPlayer sender = ctx.getOrDefault("sender", null);
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable Cache<UUID, UUID> cache = this.partyInvites.invitesFor(player.uuid());
        final @Nullable Pair<UUID, UUID> inv;
        if (cache == null) {
            inv = null;
        } else if (sender != null) {
            final @Nullable UUID i = cache.getIfPresent(sender.uuid());
            inv = i == null ? null : Pair.of(sender.uuid(), i);
        } else {
            final Map<UUID, UUID> map = Map.copyOf(cache.asMap());
            if (map.size() == 1) {
                final Map.Entry<UUID, UUID> e = map.entrySet().iterator().next();
                inv = Pair.of(e.getKey(), e.getValue());
            } else {
                player.sendMessage(Component.text("You must specify whose invite to accept.", NamedTextColor.RED));
                return;
            }
        }
        if (inv == null) {
            player.sendMessage(Component.text("You do not have a pending party invite.", NamedTextColor.RED));
            return;
        }
        final @Nullable PartyImpl party = this.userManager.party(inv.getSecond()).join();
        if (party == null) {
            player.sendMessage(Component.text("You do not have a pending party invite.", NamedTextColor.RED));
            return;
        }
        final @Nullable UUID oldPartyId = player.party();
        if (oldPartyId != null) {
            final @Nullable PartyImpl old = this.userManager.party(oldPartyId).join();
            if (old != null) {
                player.sendMessage(Component.text("You must leave your current party first.", NamedTextColor.RED));
                return;
            }
        }
        this.partyInvites.invalidateInvite(inv.getFirst(), player.uuid());
        party.addMember(player.uuid());
        player.sendMessage(Component.text("u joined " + party.name()));
    }

    private void leaveParty(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable UUID partyId = player.party();
        if (partyId != null) {
            final @Nullable PartyImpl old = this.userManager.party(partyId).join();
            if (old == null) {
                mustBeInParty(player);
                return;
            }
            if (old.members().size() == 1) {
                this.disbandParty(ctx);
                return;
            }
            old.removeMember(player.uuid());
        } else {
            mustBeInParty(player);
            return;
        }
        player.sendMessage(Component.text("u left party"));
    }

    private void disbandParty(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable UUID partyId = player.party();
        if (partyId != null) {
            final @Nullable PartyImpl old = this.userManager.party(partyId).join();
            if (old == null) {
                mustBeInParty(player);
                return;
            }
            if (old.members().size() != 1) {
                player.sendMessage(Component.text("Cannot disband, you are not the last member.", NamedTextColor.RED));
                return;
            }
            old.disband();
            player.sendMessage(Component.text("u disbanded party"));
        } else {
            mustBeInParty(player);
        }
    }

    private static void mustBeInParty(final Audience player) {
        player.sendMessage(Component.text("You must be in a party to use this command.", NamedTextColor.RED));
    }
}
