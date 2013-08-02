/*
 * MinecartComponent.java
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
import org.bukkit.Effect;
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.shackledmc.physics.Physics;
import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.api.MinecartHitEvent;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Message;
import com.shackledmc.physics.util.Experimental.ParticleEffectType;

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
    private boolean effects;
    
    public MinecartComponent() {
        super(ComponentType.MINECART);
        
        if(!enabled) return;
        
        MinecartModifier.clearCache();
        FileConfiguration configFile = Physics.getInstance().getConfig();
        minSpeedSquared = Math.pow(configFile.getDouble("minecarts.min-detected-speed"), 2);
        deathMessage = configFile.getString("minecarts.death-message");
        effects = configFile.getBoolean("minecarts.effects");
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
    
    @Override
    public void statsInit(PluginMetrics metrics) {
        Graph componentGraph = metrics.createGraph("component.minecart.enabled");
        
        componentGraph.addPlotter(new PluginMetrics.Plotter("Enabled") {

            @Override
            public int getValue() {
                if(enabled) return 1;
                else return 0;
            }

        });

        componentGraph.addPlotter(new PluginMetrics.Plotter("Disabled") {

            @Override
            public int getValue() {
                if(!enabled) return 1;
                else return 0;
            }

        });
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMinecartMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        
        if(exemptWorlds.contains(vehicle.getWorld().getName())) return;
        
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
                if(!victim.hasPermission(type.getPermission())) continue;
                
                // Process damage handling
                double damageValue = event.getVehicle().getVelocity().length() * (10 * MinecartModifier.PLAYERS.modifier);
                
                EntityDamageEvent damage = new EntityDamageByEntityEvent(vehicle, victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                
                MinecartHitEvent apiEvent = new MinecartHitEvent(vehicle, victimEntity, damageValue);
                Bukkit.getPluginManager().callEvent(apiEvent);
                if(apiEvent.isCancelled()) continue;
                
                victim.damage(damageValue);
                
                // Process death message handling
                victim.setMetadata("hitbyminecart", new FixedMetadataValue(Physics.getInstance(), true));
                
                Bukkit.getScheduler().runTaskLater(Physics.getInstance(), new Runnable() {
                    
                    @Override
                    public void run() {
                        if(!victim.hasMetadata("hitbyminecart")
                                || (victim.hasMetadata("hitbyminecart")
                                        && !victim.getMetadata("hitbyminecart").get(0).asBoolean())) return;
                        
                        victim.removeMetadata("hitbyminecart", Physics.getInstance());
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
                double damageValue = event.getVehicle().getVelocity().length() * (10 * MinecartModifier.ANIMALS.modifier);
                
                EntityDamageEvent damage = new EntityDamageByEntityEvent(vehicle, victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                
                MinecartHitEvent apiEvent = new MinecartHitEvent(vehicle, victimEntity, damageValue);
                Bukkit.getPluginManager().callEvent(apiEvent);
                if(apiEvent.isCancelled()) continue;
                
                victimEntity.damage(damageValue);
                
                // Process knockback
                if(MinecartModifier.ANIMALS.knockback) {
                    Vector velocity = victimEntity.getVelocity().clone();
                    victimEntity.setVelocity(velocity);
                    velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));
                }

            } else if(victimEntity instanceof Monster || victimEntity instanceof Slime) {
                // Process damage
                double damageValue = event.getVehicle().getVelocity().length() * (10 * MinecartModifier.MOBS.modifier);
                
                EntityDamageEvent damage = new EntityDamageByEntityEvent(vehicle, victimEntity, DamageCause.ENTITY_ATTACK, damageValue);
                Bukkit.getPluginManager().callEvent(damage);
                if(damage.isCancelled()) continue;
                
                MinecartHitEvent apiEvent = new MinecartHitEvent(vehicle, victimEntity, damageValue);
                Bukkit.getPluginManager().callEvent(apiEvent);
                if(apiEvent.isCancelled()) continue;
                
                victimEntity.damage(damageValue);
                
                // Process knockback
                if(MinecartModifier.MOBS.knockback) {
                    Vector velocity = victimEntity.getVelocity().clone();
                    victimEntity.setVelocity(velocity);
                    velocity.add(vehicle.getVelocity().multiply(2.5).add(new Vector(0,.5,0)));
                }

            } else continue;
            
            if(effects) {
                Location vehicleLoc = vehicle.getLocation();
                Experimental.createEffect(ParticleEffectType.LAVA, "", vehicleLoc, 0.5f, 0.5f, 5f, 20);
                vehicleLoc.getWorld().playEffect(vehicleLoc, Effect.ZOMBIE_DESTROY_DOOR, 0);
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
        
        player.removeMetadata("hitbyminecart", Physics.getInstance());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!player.hasMetadata("hitbyminecart")
                || (player.hasMetadata("hitbyminecart")
                        && !player.getMetadata("hitbyminecart").get(0).asBoolean())) return;
        
        player.removeMetadata("hitbyminecart", Physics.getInstance());
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
            modifier = Physics.getInstance().getConfig().getDouble("minecarts.modifiers." + key + ".damage");
            knockback = Physics.getInstance().getConfig().getBoolean("minecarts.modifiers." + key + ".knockback");
        }
        
        public static void clearCache() {
            for(MinecartModifier area : MinecartModifier.values()) area.refresh();
        }
    }
}
