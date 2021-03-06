/*
 * BoatComponent.java
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

package com.shackledmc.physics.components;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.Physics;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Message;

/**
 * Boat component.
 * 
 * Handles boat sinking when damaged
 * @author bitWolfy
 *
 */
public class BoatComponent extends Component implements Listener {
    
    private double damageMultiplyer;
    private boolean effects;
    
    public BoatComponent() {
        super(ComponentType.BOAT);
        
        if(!enabled) return;
        
        damageMultiplyer = Physics.getInstance().getConfig().getDouble("boats.damage-multiplyer");
        effects = Physics.getInstance().getConfig().getBoolean("boats.effects");
    }
    
    @Override
    public void onEnable() {
        if(effects && !Physics.isCraftBukkitCompatible()) {
            Message.log(
                    "|  |- Particle effects are not compatible with  |",
                    "|     your CraftBukkit version. Disabling...    |"
                    );
            effects = false;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, Physics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatDamage(VehicleDamageEvent event) {
        if(exemptWorlds.contains(event.getVehicle().getWorld().getName())) return;
        
        if(!(event.getVehicle() instanceof Boat)) return;
        
        Boat boat = (Boat) event.getVehicle();
        Entity passenger = boat.getPassenger();
        if (passenger == null || (passenger != null && !((Player) passenger).hasPermission(type.getPermission()))) return;
        
        if(!boat.hasMetadata("sinking") && !boat.isDead() && (event.getDamage() >= 2)) {
            boat.setMetadata("sinking", new FixedMetadataValue(Physics.getInstance(), true));
            boat.setVelocity(boat.getVelocity().subtract(new Vector(0,.05,0).multiply(damageMultiplyer)));
        }

        if(effects) Experimental.createBlockEffect(boat.getLocation(), 22);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatDestroy(VehicleDestroyEvent event) {
        if(exemptWorlds.contains(event.getVehicle().getWorld().getName())) return;
        
        Vehicle vehicle = event.getVehicle();
        if(vehicle instanceof Boat && vehicle.hasMetadata("sinking"))
            vehicle.removeMetadata("sinking", Physics.getInstance());     
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        
        if(exemptWorlds.contains(vehicle.getWorld().getName())) return;
        
        if(!(vehicle instanceof Boat) || !vehicle.hasMetadata("sinking")) return;
        
        Entity passenger = vehicle.getPassenger();
        if (passenger == null || (passenger != null && !((Player) passenger).hasPermission(type.getPermission()))) return;
        
        Material blockUnder = vehicle.getWorld().getBlockAt(event.getTo()).getRelative(0, -1, 0).getType();
        
        if(blockUnder != Material.WATER && blockUnder != Material.STATIONARY_WATER) return;
        
        Vector vec  = event.getVehicle().getVelocity();
        vec.subtract(new Vector(0,.05,0).multiply(damageMultiplyer));
        event.getVehicle().setVelocity(vec);
    }
    
}
