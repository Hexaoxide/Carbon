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
package net.draycia.carbon.fabric.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public record FabricPlayerCommander(
    CarbonChat carbon,
    CommandSourceStack commandSourceStack
) implements PlayerCommander, FabricCommander {

    public ServerPlayer player() {
        try {
            return this.commandSourceStack.getPlayerOrException();
        } catch (final CommandSyntaxException e) {
            throw new IllegalStateException("FabricPlayerCommander was created for non-player CommandSourceStack!", e);
        }
    }

    @Override
    public CarbonPlayer carbonPlayer() {
        return requireNonNull(
            this.carbon.server().userManager().carbonPlayer(this.player().getUUID()).join().player(),
            "No CarbonPlayer for logged in Player!"
        );
    }

    @Override
    public boolean hasPermission(final String permission) {
        return Permissions.check(this.commandSourceStack, permission, this.commandSourceStack.getServer().getOperatorUserPermissionLevel());
    }

}
