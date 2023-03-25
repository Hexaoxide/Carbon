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

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.users.json.JSONUserManager;

/**
 * Injection binding annotation for the backing {@link UserManagerInternal}
 * (i.e. {@link JSONUserManager} or {@link DatabaseUserManager},
 * with the generic type of {@link CarbonPlayerCommon}.
 *
 * <p>Injecting {@link UserManagerInternal} or {@link UserManager} with a generic type of {@literal ?}, without this annotation,
 * will inject the {@link PlatformUserManager}, which wraps the backing manager.</p>
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface Backing {
}
