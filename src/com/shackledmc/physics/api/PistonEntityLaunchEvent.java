/*
 * PistonEntityLaunchEvent.java
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
import lombok.Setter;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import com.shackledmc.physics.ComponentManager.ComponentType;

/**
 * An event thrown when a power piston launches an entity
 * @author bitWolfy
 *
 */
@Getter(AccessLevel.PUBLIC)
public class PistonEntityLaunchEvent extends PhysicsEvent implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    private Block piston;
    private Entity entity;
    private Vector velocity;
    
    @Setter(AccessLevel.PUBLIC)
    private boolean cancelled;
    
    public PistonEntityLaunchEvent(Block piston, Entity entity, Vector velocity) {
        super(ComponentType.PISTON);
        
        this.piston = piston;
        this.entity = entity;
        this.velocity = velocity;
        
        cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
