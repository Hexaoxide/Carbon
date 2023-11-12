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

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.Option;
import net.draycia.carbon.common.messages.TagPermissions;
import net.draycia.carbon.common.users.NetworkUsers;
import net.draycia.carbon.common.users.PartyInvites;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.Pagination;
import net.draycia.carbon.common.util.PaginationHelper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

@DefaultQualifier(NonNull.class)
public final class PartyCommands extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final ArgumentFactory argumentFactory;
    private final UserManagerInternal<?> userManager;
    private final PartyInvites partyInvites;
    private final ConfigManager config;
    private final CarbonMessages messages;
    private final PaginationHelper pagination;
    private final NetworkUsers network;

    @Inject
    public PartyCommands(
        final CommandManager<Commander> commandManager,
        final ArgumentFactory argumentFactory,
        final UserManagerInternal<?> userManager,
        final PartyInvites partyInvites,
        final ConfigManager config,
        final CarbonMessages messages,
        final PaginationHelper pagination,
        final NetworkUsers network
    ) {
        this.commandManager = commandManager;
        this.argumentFactory = argumentFactory;
        this.userManager = userManager;
        this.partyInvites = partyInvites;
        this.config = config;
        this.messages = messages;
        this.pagination = pagination;
        this.network = network;
    }

    @Override
    public void init() {
        if (!this.config.primaryConfig().partyChat().enabled) {
            return;
        }

        final var root = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .senderType(PlayerCommander.class)
            .permission("carbon.parties");
        final var info = root.commandDescription(richDescription(this.messages.partyDesc())).handler(this::info);

        this.commandManager.command(info);
        this.commandManager.command(info.literal("page")
            .optional("page", integerParser(1), DefaultValue.constant(1)));
        this.commandManager.command(
            root.literal("create")
                .commandDescription(richDescription(this.messages.partyCreateDesc()))
                .optional("name", greedyStringParser())
                .handler(this::createParty)
        );
        this.commandManager.command(
            root.literal("invite")
                .commandDescription(richDescription(this.messages.partyInviteDesc()))
                .required("player", this.argumentFactory.carbonPlayer())
                .handler(this::invitePlayer)
        );
        this.commandManager.command(
            root.literal("accept")
                .commandDescription(richDescription(this.messages.partyAcceptDesc()))
                .optional("sender", this.argumentFactory.carbonPlayer())
                .handler(this::acceptInvite)
        );
        this.commandManager.command(
            root.literal("leave")
                .commandDescription(richDescription(this.messages.partyLeaveDesc()))
                .handler(this::leaveParty)
        );
        this.commandManager.command(
            root.literal("disband")
                .commandDescription(richDescription(this.messages.partyDisbandDesc()))
                .handler(this::disbandParty)
        );
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("party", "group");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "party");
    }

    private void info(final CommandContext<PlayerCommander> ctx) {
        final CarbonPlayer player = ctx.sender().carbonPlayer();
        final @Nullable Party party = player.party().join();
        if (party == null) {
            this.messages.notInParty(player);
            return;
        }

        this.messages.currentParty(player, party.name());

        final var elements = party.members().stream()
            .sorted(Comparator.<UUID, Boolean>comparing(this.network::online).reversed().thenComparing(UUID::compareTo))
            .map(id -> (Supplier<CarbonPlayer>) () -> this.userManager.user(id).join())
            .toList();

        if (elements.isEmpty()) {
            throw new IllegalStateException();
        }

        final Pagination<Supplier<CarbonPlayer>> pagination = Pagination.<Supplier<CarbonPlayer>>builder()
            .header((page, pages) -> this.messages.commandPartyPaginationHeader(party.name()))
            .item((e, lastOfPage) -> {
                final CarbonPlayer p = e.get();
                return this.messages.commandPartyPaginationElement(p.displayName(), p.username(), new Option(this.network.online(p)));
            })
            .footer(this.pagination.footerRenderer(p -> "/" + this.commandSettings().name() + " page " + p))
            .pageOutOfRange(this.messages::paginationOutOfRange)
            .build();

        final int page = ctx.getOrDefault("page", 1);

        pagination.render(elements, page, 6).forEach(player::sendMessage);
    }

    private void createParty(final CommandContext<PlayerCommander> ctx) {
        final CarbonPlayer player = ctx.sender().carbonPlayer();
        final @Nullable Party oldParty = player.party().join();
        if (oldParty != null) {
            this.messages.mustLeavePartyFirst(player);
            return;
        }
        final String name = ctx.getOrDefault("name", player.username() + "'s party");
        final Component component = TagPermissions.parseTags(TagPermissions.PARTY_NAME, name, player::hasPermission);
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

    private void invitePlayer(final CommandContext<PlayerCommander> ctx) {
        final CarbonPlayer player = ctx.sender().carbonPlayer();
        final CarbonPlayer recipient = ctx.get("player");
        if (recipient.uuid().equals(player.uuid())) {
            this.messages.cannotInviteSelf(player);
            return;
        }
        final @Nullable Party party = player.party().join();
        if (party == null) {
            this.messages.mustBeInParty(player);
            return;
        }
        final @Nullable Party recipientParty = recipient.party().join();
        if (recipientParty != null && recipientParty.id().equals(party.id())) {
            this.messages.alreadyInParty(player, recipient.displayName());
            return;
        }
        this.partyInvites.sendInvite(player.uuid(), recipient.uuid(), party.id());
        this.messages.receivedPartyInvite(recipient, player.displayName(), player.username(), party.name());
        this.messages.sentPartyInvite(player, recipient.displayName(), party.name());
    }

    private void acceptInvite(final CommandContext<PlayerCommander> ctx) {
        final @Nullable CarbonPlayer sender = ctx.getOrDefault("sender", null);
        final CarbonPlayer player = ctx.sender().carbonPlayer();
        final @Nullable Invite invite = this.findInvite(player, sender);
        if (invite == null) {
            return;
        }
        final @Nullable Party old = player.party().join();
        if (old != null) {
            this.messages.mustLeavePartyFirst(player);
            return;
        }
        this.partyInvites.invalidateInvite(invite.sender(), player.uuid());
        invite.party().addMember(player.uuid());
        this.messages.joinedParty(player, invite.party().name());
    }

    private void leaveParty(final CommandContext<PlayerCommander> ctx) {
        final CarbonPlayer player = ctx.sender().carbonPlayer();
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

    private void disbandParty(final CommandContext<PlayerCommander> ctx) {
        final CarbonPlayer player = ctx.sender().carbonPlayer();
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

    private @Nullable Invite findInvite(final CarbonPlayer player, final @Nullable CarbonPlayer sender) {
        final @Nullable Cache<UUID, UUID> cache = this.partyInvites.invitesFor(player.uuid());
        final @Nullable Map<UUID, UUID> map = cache != null ? Map.copyOf(cache.asMap()) : null;

        if (map == null || map.isEmpty()) {
            this.messages.noPendingPartyInvites(player);
            return null;
        } else if (sender != null) {
            final @Nullable Party p = Optional.ofNullable(map.get(sender.uuid()))
                .map(id -> this.userManager.party(id).join())
                .orElse(null);
            if (p == null) {
                this.messages.noPartyInviteFrom(player, sender.displayName());
                return null;
            }
            return new Invite(sender.uuid(), p);
        }

        if (map.size() == 1) {
            final Map.Entry<UUID, UUID> e = map.entrySet().iterator().next();
            final @Nullable Party p = this.userManager.party(e.getValue()).join();
            if (p == null) {
                this.messages.noPendingPartyInvites(player);
                return null;
            }
            return new Invite(e.getKey(), p);
        }

        this.messages.mustSpecifyPartyInvite(player);
        return null;
    }

    private record Invite(UUID sender, Party party) {}

}
