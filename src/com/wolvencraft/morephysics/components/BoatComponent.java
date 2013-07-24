/*
 * BoatComponent.java
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

package com.wolvencraft.morephysics.components;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.PluginComponent;

/**
 * Boat component.
 * 
 * Handles boat sinking when damaged
 * @author bitWolfy
 *
 */
public class BoatComponent extends Component implements Listener {
    
    private List<Boat> sinkingBoats;
    
    public BoatComponent() {
        super(PluginComponent.BOAT);
        
        if(!enabled) return;
        sinkingBoats = new ArrayList<Boat>();
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatDamage(VehicleDamageEvent event) {
        if(!(event.getVehicle() instanceof Boat)) return;
        
        Boat boat = (Boat) event.getVehicle();
        Entity passenger = boat.getPassenger();
        if (passenger == null || (passenger != null && ((Player) passenger).hasPermission(permission))) return;
        
        if(!sinkingBoats.contains(boat) && !boat.isDead() && (event.getDamage() >= 2)) {
            sinkingBoats.add(boat);
            boat.setVelocity(boat.getVelocity().subtract(new Vector(0,.05,0)));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatDestroy(VehicleDestroyEvent event) {
        if(event.getVehicle() instanceof Boat && sinkingBoats.contains((Boat) event.getVehicle()))
            sinkingBoats.remove((Boat) event.getVehicle());        
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        
        if(!(vehicle instanceof Boat)
                || !(sinkingBoats.contains((Boat) vehicle))) return;
        
        Entity passenger = vehicle.getPassenger();
        if (passenger == null || (passenger != null && ((Player) passenger).hasPermission(permission))) return;
        
        Material blockUnder = vehicle.getWorld().getBlockAt(event.getTo()).getRelative(0, -1, 0).getType();
        
        if(blockUnder != Material.WATER && blockUnder != Material.STATIONARY_WATER) return;
        
        Vector vec  = event.getVehicle().getVelocity();
        vec.subtract(new Vector(0,.05,0));
        event.getVehicle().setVelocity(vec);
    }
    
}
