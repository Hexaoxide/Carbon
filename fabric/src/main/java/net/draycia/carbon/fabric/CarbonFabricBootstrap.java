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
package net.draycia.carbon.fabric;

import com.google.inject.Guice;
import net.draycia.carbon.api.CarbonChatProvider;
import net.fabricmc.api.ModInitializer;

public class CarbonFabricBootstrap implements ModInitializer {

    @Override
    public void onInitialize() {
        final CarbonChatFabric carbonChat = Guice.createInjector(new CarbonChatFabricModule())
            .getInstance(CarbonChatFabric.class);
        CarbonChatProvider.register(carbonChat);
        carbonChat.onInitialize();
    }

}
