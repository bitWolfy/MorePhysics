/*
 * ArrowComponent.java
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

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.shackledmc.physics.Physics;
import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.api.ProjectileCritEvent;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Message;
import com.shackledmc.physics.util.Experimental.ParticleEffectType;

/**
 * Arrow damage component.
 * 
 * Handles critical hits done by arrows.
 * @author bitWolfy
 *
 */
public class ArrowComponent extends Component implements Listener {
    
    private boolean effects;
    
    public ArrowComponent() {
        super(ComponentType.ARROW);
        
        if(!enabled) return;
        
        HitArea.clearCache();
        effects = Physics.getInstance().getConfig().getBoolean("arrows.effects");
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
        Graph componentGraph = metrics.createGraph("component.arrow.enabled");
        
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Projectile)
            || !(event.getEntity() instanceof Player)
            || !((Player) event.getEntity()).hasPermission(type.getPermission())) return;
        
        if(exemptWorlds.contains(event.getEntity().getWorld().getName())) return;
        
        HitArea hitArea;
        
        double diff = event.getDamager().getLocation().getY() - event.getEntity().getLocation().getY();
        
        if(diff <= 1.85 && diff > 1.38) hitArea = HitArea.HEAD;
        else if(diff <= 1.38 && diff > 1.26) hitArea = HitArea.NECK;
        else if(diff <= 1.26 && diff > 0.74) hitArea = HitArea.TORSO;
        else if(diff <= 0.74 && diff > 0.46) hitArea = HitArea.CROTCH;
        else if(diff <= 0.46 && diff > 0.17) hitArea = HitArea.LEGS;
        else if(diff <= 0.17 && diff > 0) hitArea = HitArea.TOE;
        else hitArea = HitArea.UNKNOWN;
        
        ProjectileCritEvent apiEvent = new ProjectileCritEvent(event, hitArea);
        Bukkit.getServer().getPluginManager().callEvent(apiEvent);
        if(apiEvent.isCancelled()) return;
        
        if(effects && (hitArea.equals(HitArea.HEAD) || hitArea.equals(HitArea.NECK) || hitArea.equals(HitArea.CROTCH))) {
            Experimental.createEffect(ParticleEffectType.CRIT, "", event.getEntity().getLocation(), 1f, 1f, 1f, 20);
        }
        
        event.setDamage(event.getDamage() * hitArea.modifier);
    }
    
    /**
     * Represents the area that was hit by a projectile
     * @author bitWolfy
     *
     */
    @Getter(AccessLevel.PUBLIC)
    public enum HitArea {
        
        HEAD    ("head"),
        NECK    ("neck"),
        TORSO   ("torso"),
        CROTCH  ("crotch"),
        LEGS    ("legs"),
        TOE     ("toe"),
        
        UNKNOWN ("unknown"),
        ;
        
        private String key;
        private double modifier;
        
        HitArea(String key) {
            this.key = key;
            refresh();
        }
        
        private void refresh() {
            modifier = Physics.getInstance().getConfig().getDouble("arrows.modifiers." + key);
        }
        
        public static void clearCache() {
            for(HitArea area : HitArea.values()) area.refresh();
        }
        
    }
    
}
