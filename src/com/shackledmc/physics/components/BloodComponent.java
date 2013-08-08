/*
 * BloodComponent.java
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
import lombok.AllArgsConstructor;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.material.MaterialData;

import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.Physics;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Message;
import com.shackledmc.physics.util.Util;

/**
 * Blood component.
 * 
 * Handles blood particle effects
 * @author bitWolfy
 *
 */
public class BloodComponent extends Component implements Listener {
    
    public BloodComponent() {
        super(ComponentType.BLOOD);
        
        if(!enabled) return;
        
        BloodModifier.clearCache();
    }
    
    @Override
    public void onEnable() {
        if(!Physics.isCraftBukkitCompatible()) {
            Message.log(
                    "|  |- Component is not compatible with your     |",
                    "|     CraftBukkit version. Disabling...         |",
                    "| [X] BloodComponent is disabled                |"
                    );
            enabled = false;
            return;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, Physics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        
        if(exemptWorlds.contains(event.getEntity().getWorld().getName())) return;
        
        Entity entity = event.getEntity();
        if(entity instanceof Player) {
            if(!BloodModifier.PLAYERS.enabled) return;
            
            Experimental.createBlockEffect(entity.getLocation(), BloodModifier.PLAYERS.color.blockId);
        } else if(entity instanceof Animals) {
            if(!BloodModifier.ANIMALS.enabled) return;
            
            Experimental.createBlockEffect(entity.getLocation(), BloodModifier.ANIMALS.color.blockId);
        } else if(entity instanceof Monster || entity instanceof Slime) {
            if(!BloodModifier.MOBS.enabled) return;
            
            Experimental.createBlockEffect(entity.getLocation(), BloodModifier.MOBS.color.blockId);
            
        }
    }
    
    private enum BloodModifier {
        
        PLAYERS         ("players"),
        MOBS            ("mobs"),
        ANIMALS         ("animals"),
        ;
        
        private String key;
        
        private boolean enabled;
        private BloodColor color;
        
        BloodModifier(String key) {
            this.key = key;
        }
        
        private void refresh() {
            FileConfiguration configFile = Physics.getInstance().getConfig();
            
            enabled = configFile.getBoolean("blood.types." + key + ".enabled");
            
            color = BloodColor.get(this, configFile.getString("blood.types." + key + ".color"));
            if(color == null) enabled = false;
        }
        
        public static void clearCache() {
            for(BloodModifier area : BloodModifier.values()) area.refresh();
        }
    }
    
    @AllArgsConstructor(access=AccessLevel.PRIVATE)
    private enum BloodColor {
        
        WHITE           ("white", 155),
        WHITE_ALT       ("white_alt", 42),
        RED             ("red_alt", 152),
        RED_ALT         ("red_alt_2", 87),
        YELLOW          ("yellow", 41),
        YELLOW_ALT      ("yellow_alt", 42),
        BLUE            ("blue", 22),
        BLUE_ALT        ("blue_alt", 57),
        GREEN           ("green", 133),
        GREEN_ALT       ("green_alt", 81),
        GLITTER         ("glitter", 20),
        
        CUSTOM_PLAYER   ("CUSTOM_PLAYER", 1),
        CUSTOM_ANIMAL   ("CUSTOM_ANIMAL", 1),
        CUSTOM_MOB      ("CUSTOM_MOB", 1),
        ;
        
        private String alias;
        private int blockId;
        
        public static BloodColor get(BloodModifier modifier, String alias) {
            for(BloodColor color : BloodColor.values()) {
                if(color.alias.equalsIgnoreCase(alias)) return color;
            }
            
            MaterialData customData = Util.getBlockMaterial(alias);
            if(customData == null) return BloodColor.RED;
            
            BloodColor result;
            switch(modifier) {
                case PLAYERS: {
                    result = BloodColor.CUSTOM_PLAYER;
                    result.blockId = customData.getItemTypeId();
                    break;
                }
                
                case MOBS: {
                    result = BloodColor.CUSTOM_MOB;
                    result.blockId = customData.getItemTypeId();
                    break;
                }
                
                case ANIMALS: {
                    result = BloodColor.CUSTOM_ANIMAL;
                    result.blockId = customData.getItemTypeId();
                    break;
                }
                
                default: {
                    result = BloodColor.RED;
                }
            }
            
            return result;
        }
        
    }
    
}
