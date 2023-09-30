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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.types.tuples.Pair;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import java.util.Map;
import java.util.UUID;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.PartyInvites;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PartyCommands extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final ArgumentFactory argumentFactory;
    private final UserManagerInternal<?> userManager;
    private final PartyInvites partyInvites;
    private final ConfigManager config;
    private final CarbonMessages messages;

    @Inject
    public PartyCommands(
        final CommandManager<Commander> commandManager,
        final ArgumentFactory argumentFactory,
        final UserManagerInternal<?> userManager,
        final PartyInvites partyInvites,
        final ConfigManager config,
        final CarbonMessages messages
    ) {
        this.commandManager = commandManager;
        this.argumentFactory = argumentFactory;
        this.userManager = userManager;
        this.partyInvites = partyInvites;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void init() {
        if (!this.config.primaryConfig().partyChat()) {
            return;
        }

        final var root = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.parties");
        this.commandManager.command(
            root.meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.partyDesc())
                .handler(this::info)
        );
        this.commandManager.command(
            root.literal("create")
                .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.partyCreateDesc())
                .argument(StringArgument.<Commander>builder("name").greedy().asOptional())
                .senderType(PlayerCommander.class)
                .handler(this::createParty)
        );
        this.commandManager.command(
            root.literal("invite")
                .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.partyInviteDesc())
                .senderType(PlayerCommander.class)
                .argument(this.argumentFactory.carbonPlayer("player"))
                .handler(this::invitePlayer)
        );
        this.commandManager.command(
            root.literal("accept")
                .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.partyAcceptDesc())
                .senderType(PlayerCommander.class)
                .argument(this.argumentFactory.carbonPlayer("sender").asOptional())
                .handler(this::acceptInvite)
        );
        this.commandManager.command(
            root.literal("leave")
                .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.partyLeaveDesc())
                .senderType(PlayerCommander.class)
                .handler(this::leaveParty)
        );
        this.commandManager.command(
            root.literal("disband")
                .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.partyDisbandDesc())
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
        final @Nullable Party party = player.party().join();
        if (party == null) {
            this.messages.notInParty(player);
        } else {
            this.messages.currentParty(player, party.name());
        }
    }

    private void createParty(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable Party oldParty = player.party().join();
        if (oldParty != null) {
            this.messages.mustLeavePartyFirst(player);
            return;
        }
        final String name = ctx.getOrDefault("name", player.username() + "'s party");
        final Component component = ((WrappedCarbonPlayer) player).parseMessageTags(name);
        final Party party;
        try {
            party = this.userManager.createParty(component);
        } catch (final IllegalArgumentException e) {
            this.messages.partyNameTooLong(player);
            return;
        }
        party.addMember(player.uuid());
        this.messages.partyCreated(player, party.name());
    }

    private void invitePlayer(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final CarbonPlayer recipient = ctx.get("player");
        final @Nullable Party party = player.party().join();
        if (party == null) {
            this.messages.mustBeInParty(player);
            return;
        }
        this.partyInvites.sendInvite(player.uuid(), recipient.uuid(), party.id());
        this.messages.receivedPartyInvite(recipient, player.displayName(), party.name());
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
                this.messages.mustSpecifyPartyInvite(player);
                return;
            }
        }
        if (inv == null) {
            this.messages.noPendingPartyInvites(player);
            return;
        }
        final @Nullable Party party = this.userManager.party(inv.getSecond()).join();
        if (party == null) {
            this.messages.noPendingPartyInvites(player);
            return;
        }
        final @Nullable Party old = player.party().join();
        if (old != null) {
            this.messages.mustLeavePartyFirst(player);
            return;
        }
        this.partyInvites.invalidateInvite(inv.getFirst(), player.uuid());
        party.addMember(player.uuid());
        this.messages.joinedParty(player, party.name());
    }

    private void leaveParty(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable Party old = player.party().join();
        if (old == null) {
            this.messages.mustBeInParty(player);
            return;
        }
        if (old.members().size() == 1) {
            this.disbandParty(ctx);
            return;
        }
        old.removeMember(player.uuid());
        this.messages.leftParty(player, old.name());
    }

    private void disbandParty(final CommandContext<Commander> ctx) {
        final CarbonPlayer player = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final @Nullable Party old = player.party().join();
        if (old == null) {
            this.messages.mustBeInParty(player);
            return;
        }
        if (old.members().size() != 1) {
            this.messages.cannotDisbandParty(player, old.name());
            return;
        }
        old.disband();
        this.messages.disbandedParty(player, old.name());
    }

}
