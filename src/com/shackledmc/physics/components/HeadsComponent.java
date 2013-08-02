/*
 * HeadsComponent.java
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

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.shackledmc.physics.Physics;
import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;

/**
 * Heads component.
 * 
 * Manages heads dropped by players
 * @author bitWolfy
 *
 */
public class HeadsComponent extends Component implements Listener {
    
    private double chance;
    
    public HeadsComponent() {
        super(ComponentType.HEADS);
        
        if(!enabled) return;
        
        FileConfiguration configFile = Physics.getInstance().getConfig();
        chance = configFile.getDouble("heads.chance");
        
        if(chance > 1) chance = 1;
        if(chance < 0) chance = 0;
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
        Graph componentGraph = metrics.createGraph("component.heads.enabled");
        
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(!player.hasPermission(type.getPermission())) return;
        
        if(new Random().nextDouble() >= chance) return;
        
        ItemStack stack = new ItemStack(Material.SKULL_ITEM);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwner(player.getName());
        stack.setItemMeta(meta);
        
        event.getDrops().add(stack);
    }
    
}
