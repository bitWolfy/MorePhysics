/*
 * ProjectileCritEvent.java
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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.components.ArrowComponent.HitArea;

/**
 * An event thrown when a player receives a critical hit
 * @author bitWolfy
 *
 */
@Getter(AccessLevel.PUBLIC)
public class ProjectileCritEvent extends PhysicsEvent {
    
    private static final HandlerList handlers = new HandlerList();
    
    private Entity damager;
    private Player damagee;
    private HitArea area;
    private double damage;
    
    public ProjectileCritEvent(Entity damager, Player damagee, HitArea area, double damage) {
        super(ComponentType.ARROW);
        
        this.damager = damager;
        this.damagee = damagee;
        this.area = area;
        this.damage = damage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
