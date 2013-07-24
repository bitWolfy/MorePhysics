/*
 * PhysicsUtil.java
 * 
 * MorePhysics
 * Copyright (C) 2013 FriedTaco, bitWolfy, and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.wolvencraft.morephysics.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * A set of methods used to perform physics-related operations
 * @author bitWolfy
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class PhysicsUtil {
    
    /**
     * Checks if the entity is roughly near the location.<br />
     * Near is defined as 1 block away from the location.
     * @param entity Entity to check
     * @param location Location to check
     * @return <b>true</b> if the entity is near the location, <b>false</b> otherwise
     */
    public static boolean isEntityNearby(Entity entity, Location location) {
        Location entityLoc = entity.getLocation();
        return entityLoc.distanceSquared(location) <= 1
                || entityLoc.clone().add(0, 1, 0).distanceSquared(location) <= 1;
    }
    
    public static void setPlayerSpeed(Player player, float speed) {
        
    }
    
    public static float getPlayerSpeed(Player player) {
        return -1;
    }
    
}
