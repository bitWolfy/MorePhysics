/*
 * MinecartHitEvent.java
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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.HandlerList;

import com.shackledmc.physics.ComponentManager.ComponentType;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * An event thrown when a minecart hits an entity
 * @author bitWolfy
 *
 */
@Getter(AccessLevel.PUBLIC)
public class MinecartHitEvent extends MorePhysicsEvent {
    
    private static final HandlerList handlers = new HandlerList();
    
    private Minecart minecart;
    private Entity entity;
    private double damage;
    
    public MinecartHitEvent(Minecart minecart, Entity entity, double damage) {
        super(ComponentType.MINECART);
        
        this.minecart = minecart;
        this.entity = entity;
        this.damage = damage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
