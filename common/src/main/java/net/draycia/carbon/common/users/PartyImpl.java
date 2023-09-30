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
package net.draycia.carbon.common.users;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PartyImpl implements Party {

    private final String name;
    private final UUID id;
    private final Set<UUID> members;
    private final Map<UUID, ChangeType> changes;
    private final UserManagerInternal<?> userManager;
    private volatile boolean disbanded = false;

    public PartyImpl(
        final String name,
        final UUID id,
        final Set<UUID> members,
        final Map<UUID, ChangeType> changes,
        final @Nullable UserManagerInternal<?> userManager
    ) {
        this.name = name;
        this.id = id;
        this.members = members;
        this.changes = changes;

        // todo
        if (userManager == null) {
            this.userManager = (UserManagerInternal<?>) CarbonChatProvider.carbonChat().userManager();
        } else {
            this.userManager = userManager;
        }
    }

    public static PartyImpl create(
        final String name,
        final UserManagerInternal<?> userManager
    ) {
        if (name.toCharArray().length > 256) {
            throw new IllegalArgumentException("Party name is too long: '%s', %s > 256".formatted(name, name.toCharArray().length));
        }
        return new PartyImpl(name, UUID.randomUUID(), ConcurrentHashMap.newKeySet(), new ConcurrentHashMap<>(), userManager);
    }

    @Override
    public void addMember(final UUID id) {
        if (this.disbanded) {
            throw new IllegalStateException("This party was disbanded.");
        }
        this.members.add(id);
        this.changes.put(id, ChangeType.ADD);
        this.userManager.saveParty(this);
        this.userManager.user(id)
            .thenAccept(user -> {
                final @Nullable UUID oldPartyId = user.party();
                user.party(this.id);
                if (oldPartyId != null) {
                    this.userManager.party(oldPartyId).thenAccept(old -> {
                        if (old != null) {
                            old.removeMember(user.uuid());
                        }
                    });
                }
            })
            // todo
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }

    @Override
    public void removeMember(final UUID id) {
        if (this.disbanded) {
            throw new IllegalStateException("This party was disbanded.");
        }
        this.members.remove(id);
        this.changes.put(id, ChangeType.REMOVE);
        this.userManager.saveParty(this);
        this.userManager.user(id)
            .thenAccept(user -> {
                if (Objects.equals(user.party(), this.id)) {
                    user.party(null);
                }
            })
            // todo
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }

    @Override
    public Set<UUID> members() {
        if (this.disbanded) {
            throw new IllegalStateException("This party was disbanded.");
        }
        return Set.copyOf(this.members);
    }

    @Override
    public void disband() {
        if (this.disbanded) {
            throw new IllegalStateException("This party is already disbanded.");
        }
        final List<? extends CarbonPlayer> players = CarbonChatProvider.carbonChat().server().players(); // todo
        players.stream().filter(p -> this.members.contains(p.uuid())).forEach(p -> p.party(null));
        this.userManager.disbandParty(this.id);
        this.disbanded = true;
    }

    public Set<UUID> rawMembers() {
        return this.members;
    }

    public Map<UUID, ChangeType> pollChanges() {
        final Map<UUID, ChangeType> ret = Map.copyOf(this.changes);
        ret.forEach((id, t) -> this.changes.remove(id));
        return ret;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public String toString() {
        return "PartyImpl[" +
            "name=" + this.name + ", " +
            "id=" + this.id + ", " +
            "members=" + this.members + ", " +
            "changes=" + this.changes + ']';
    }

    public enum ChangeType {
        ADD, REMOVE
    }

}
