/*
 * MinecartComponent.java
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
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.PluginComponent;

/**
 * Minecart component.
 * 
 * Handles players being hit by the the minecarts
 * @author bitWolfy
 *
 */
public class MinecartComponent extends Component implements Listener {
    
    private List<String> hitPlayers;
    
    private double minSpeedSquared;
    private String deathMessage;
    
    public MinecartComponent() {
        super(PluginComponent.MINECART);
        
        if(!enabled) return;
        
        MinecartModifier.clearCache();
        FileConfiguration configFile = MorePhysics.getInstance().getConfig();
        minSpeedSquared = Math.pow(configFile.getDouble("minecarts.min-detected-speed"), 2);
        deathMessage = configFile.getString("minecarts.death-message");
        
        hitPlayers = new ArrayList<String>();
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMinecartMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        
        if(!(vehicle instanceof Minecart)) return;
        
        if(vehicle.getVelocity().lengthSquared() <= minSpeedSquared) return;
        Entity cartPassenger = vehicle.getPassenger();
        
        for(Entity entity : vehicle.getNearbyEntities(.4, .75, .4)) {
            if(!(entity instanceof LivingEntity)) continue;
            
            LivingEntity victimEntity = (LivingEntity) entity;
            if(cartPassenger.equals(victimEntity)) continue;
            
            Location loc = victimEntity.getLocation();
            if(loc.distanceSquared(event.getTo()) > loc.distanceSquared(event.getFrom())) continue;
            
            Vector velocity = victimEntity.getVelocity();
                
            if(victimEntity instanceof Player) {
                Player victim = (Player) victimEntity;
                if(victim.hasPermission(permission)) continue;
                int damageValue = (int) (event.getVehicle().getVelocity().length() * (10 * MinecartModifier.PLAYERS.modifier));
                EntityDamageEvent damage = new EntityDamageEvent(victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                victim.damage(damageValue);
                if(damageValue < victim.getHealth()) continue;
                hitPlayers.add(victim.getName());
                velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));
                
                if(MinecartModifier.PLAYERS.knockback) victimEntity.setVelocity(velocity);
            } else if(victimEntity instanceof Animals) {
                int damageValue = (int) (event.getVehicle().getVelocity().length() * (10 * MinecartModifier.ANIMALS.modifier));
                EntityDamageEvent damage = new EntityDamageEvent(victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                victimEntity.damage(damageValue);
                velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));

                if(MinecartModifier.ANIMALS.knockback) victimEntity.setVelocity(velocity);
            } else if(victimEntity instanceof Monster || victimEntity instanceof Slime) {
                int damageValue = (int) (event.getVehicle().getVelocity().length() * (10 * MinecartModifier.MOBS.modifier));
                EntityDamageEvent damage = new EntityDamageEvent(victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                victimEntity.damage(damageValue);
                velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));

                if(MinecartModifier.MOBS.knockback) victimEntity.setVelocity(velocity);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if(hitPlayers.contains(p.getName())) {
            event.setDeathMessage(deathMessage.replaceAll("<PLAYER>", p.getName()));
            hitPlayers.remove(p.getName());
        }
        
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if(hitPlayers.contains(event.getPlayer().getName()))
            hitPlayers.remove(event.getPlayer().getName());
    }
    
    private enum MinecartModifier {
        
        PLAYERS         ("players"),
        MOBS            ("mobs"),
        ANIMALS         ("animals"),
        ;
        
        private String key;
        private double modifier;
        private boolean knockback;
        
        MinecartModifier(String key) {
            this.key = key;
        }
        
        private void refresh() {
            modifier = MorePhysics.getInstance().getConfig().getDouble("minecarts.modifiers." + key + ".damage");
            knockback = MorePhysics.getInstance().getConfig().getBoolean("minecarts.modifiers." + key + ".knockback");
        }
        
        public static void clearCache() {
            for(MinecartModifier area : MinecartModifier.values()) area.refresh();
        }
    }
}
