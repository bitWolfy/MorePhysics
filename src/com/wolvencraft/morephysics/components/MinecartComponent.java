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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
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
    
    private double minSpeedSquared;
    private String deathMessage;
    
    public MinecartComponent() {
        super(PluginComponent.MINECART);
        
        if(!enabled) return;
        
        MinecartModifier.clearCache();
        FileConfiguration configFile = MorePhysics.getInstance().getConfig();
        minSpeedSquared = Math.pow(configFile.getDouble("minecarts.min-detected-speed"), 2);
        deathMessage = configFile.getString("minecarts.death-message");
        
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
    }
    
    @SuppressWarnings("deprecation")
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
            
                
            if(victimEntity instanceof Player) {
                final Player victim = (Player) victimEntity;
                
                // Process permissions
                if(victim.hasPermission(permission)) continue;
                
                // Process damage handling
                int damageValue = (int) (event.getVehicle().getVelocity().length() * (10 * MinecartModifier.PLAYERS.modifier));
                EntityDamageEvent damage = new EntityDamageByEntityEvent(vehicle, victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                victim.damage(damageValue);
                
                // Process death message handling
                victim.setMetadata("hitbyminecart", new FixedMetadataValue(MorePhysics.getInstance(), true));
                
                Bukkit.getScheduler().runTaskLater(MorePhysics.getInstance(), new Runnable() {
                    
                    @Override
                    public void run() {
                        if(!victim.hasMetadata("hitbyminecart")
                                || (victim.hasMetadata("hitbyminecart")
                                        && !victim.getMetadata("hitbyminecart").get(0).asBoolean())) return;
                        
                        victim.removeMetadata("hitbyminecart", MorePhysics.getInstance());
                    }
                    
                }, 20L);
                
                // Process knockback
                if(MinecartModifier.PLAYERS.knockback) {
                    Vector velocity = victimEntity.getVelocity().clone();
                    velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));
                    victimEntity.setVelocity(velocity);
                }
                
            } else if(victimEntity instanceof Animals) {
                // Process damage
                int damageValue = (int) (event.getVehicle().getVelocity().length() * (10 * MinecartModifier.ANIMALS.modifier));
                EntityDamageEvent damage = new EntityDamageByEntityEvent(vehicle, victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                victimEntity.damage(damageValue);
                
                // Process knockback
                if(MinecartModifier.ANIMALS.knockback) {
                    Vector velocity = victimEntity.getVelocity().clone();
                    victimEntity.setVelocity(velocity);
                    velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));
                }

            } else if(victimEntity instanceof Monster || victimEntity instanceof Slime) {
                // Process damage
                int damageValue = (int) (event.getVehicle().getVelocity().length() * (10 * MinecartModifier.MOBS.modifier));
                EntityDamageEvent damage = new EntityDamageByEntityEvent(vehicle, victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                victimEntity.damage(damageValue);
                
                // Process knockback
                if(MinecartModifier.MOBS.knockback) {
                    Vector velocity = victimEntity.getVelocity().clone();
                    victimEntity.setVelocity(velocity);
                    velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(!player.hasMetadata("hitbyminecart")
                || (player.hasMetadata("hitbyminecart")
                        && !player.getMetadata("hitbyminecart").get(0).asBoolean())) return;
        
        event.setDeathMessage(deathMessage.replaceAll("<PLAYER>", player.getName()));
        
        player.removeMetadata("hitbyminecart", MorePhysics.getInstance());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!player.hasMetadata("hitbyminecart")
                || (player.hasMetadata("hitbyminecart")
                        && !player.getMetadata("hitbyminecart").get(0).asBoolean())) return;
        
        player.removeMetadata("hitbyminecart", MorePhysics.getInstance());
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
