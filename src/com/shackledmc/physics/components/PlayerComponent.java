/*
 * PlayerComponent.java
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
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import com.shackledmc.physics.Physics;
import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;

/**
 * Player component.
 * 
 * Handles various configurable player aspects
 * @author bitWolfy
 *
 */
public class PlayerComponent extends Component implements Listener {
    
    public PlayerComponent() {
        super(ComponentType.PLAYER);
        
        if(!enabled) return;
        
        PlayerModifier.clearCache();
    }
    
    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Physics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    
    @Override
    public void statsInit(PluginMetrics metrics) {
        Graph componentGraph = metrics.createGraph("component.player.enabled");
        
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
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(exemptWorlds.contains(event.getEntity().getWorld().getName())) return;
        
        event.setFoodLevel((int) (event.getFoodLevel() * PlayerModifier.RATE_HUNGER.modifier));
    }
    
    @EventHandler
    public void onExperienceGain(PlayerExpChangeEvent event) {
        if(exemptWorlds.contains(event.getPlayer().getWorld().getName())) return;
        
        event.setAmount((int) (event.getAmount() * PlayerModifier.RATE_EXPERIENCE.modifier));
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(exemptWorlds.contains(event.getEntity().getWorld().getName())) return;
        
        Entity damager = event.getDamager();
        if(damager instanceof Player) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_PVP.modifier);
        } else if(damager instanceof Monster
               || damager instanceof Slime
               || damager instanceof EnderDragon
               || damager instanceof Wither) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_PVE.modifier);
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(exemptWorlds.contains(event.getEntity().getWorld().getName())) return;
        
        DamageCause cause = event.getCause();
        if(cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK)) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_FIRE.modifier);
        } else if(cause.equals(DamageCause.SUFFOCATION)) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_SUFFOCATION.modifier);
        } else if(cause.equals(DamageCause.STARVATION)) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_STARVATION.modifier);
        } else if(cause.equals(DamageCause.POISON)) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_POISON.modifier);
        } else if(cause.equals(DamageCause.FALL)) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_FALL.modifier);
        } else if(cause.equals(DamageCause.BLOCK_EXPLOSION) || cause.equals(DamageCause.ENTITY_EXPLOSION)) {
            event.setDamage(event.getDamage() * PlayerModifier.DAMAGE_EXPLOSION.modifier);
        }
    }
    
    private enum PlayerModifier {

        RATE_HUNGER             ("rates.hunger"),
        RATE_EXPERIENCE         ("rates.experience"),
        DAMAGE_PVP              ("damage.pvp"),
        DAMAGE_PVE              ("damage.pve"),
        DAMAGE_FIRE             ("damage.fire"),
        DAMAGE_SUFFOCATION      ("damage.suffocation"),
        DAMAGE_STARVATION       ("damage.starvation"),
        DAMAGE_POISON           ("damage.posion"),
        DAMAGE_FALL             ("damage.fall"),
        DAMAGE_EXPLOSION        ("damage.explosion"),
        ;
        
        private String key;
        private double modifier;
        
        PlayerModifier(String key) {
            this.key = key;
            refresh();
        }
        
        private void refresh() {
            modifier = Physics.getInstance().getConfig().getDouble("player.modifiers." + key);
        }
        
        public static void clearCache() {
            for(PlayerModifier area : PlayerModifier.values()) area.refresh();
        }
        
    }
    
}
