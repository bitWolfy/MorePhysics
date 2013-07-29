/*
 * ArrowComponent.java
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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.ComponentType;
import com.wolvencraft.morephysics.util.Experimental;
import com.wolvencraft.morephysics.util.Message;
import com.wolvencraft.morephysics.util.Experimental.ParticleEffectType;

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
        effects = MorePhysics.getInstance().getConfig().getBoolean("arrows.effects");
    }
    
    @Override
    public void onEnable() {
        if(effects && !MorePhysics.isCraftBukkitCompatible()) {
            Message.log(
                    "|  |- Particle effects are not compatible with  |",
                    "|     your CraftBukkit version. Disabling...    |"
                    );
            effects = false;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
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
        
        if(effects && (hitArea.equals(HitArea.HEAD) || hitArea.equals(HitArea.NECK) || hitArea.equals(HitArea.CROTCH))) {
            Experimental.createEffect(ParticleEffectType.CRIT, "", event.getEntity().getLocation(), 1f, 1f, 1f, 20);
        }
        
        event.setDamage(event.getDamage() * hitArea.modifier);
    }
    
    private enum HitArea {
        
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
            modifier = MorePhysics.getInstance().getConfig().getDouble("arrows.modifiers." + key);
        }
        
        public static void clearCache() {
            for(HitArea area : HitArea.values()) area.refresh();
        }
        
    }
    
}
