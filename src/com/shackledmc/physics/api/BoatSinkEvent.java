/*
 * BoatSinkEvent.java
 * 
 * Physics
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

package com.shackledmc.physics.api;

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.entity.Boat;
import org.bukkit.event.HandlerList;

import com.shackledmc.physics.ComponentManager.ComponentType;

/**
 * An event thrown when a boat takes enough damage to sink
 * @author bitWolfy
 *
 */
@Getter(AccessLevel.PUBLIC)
public class BoatSinkEvent extends MorePhysicsEvent {
    
    private static final HandlerList handlers = new HandlerList();
    
    private Boat boat;
    
    public BoatSinkEvent(Boat boat) {
        super(ComponentType.BOAT);
        
        this.boat = boat;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
