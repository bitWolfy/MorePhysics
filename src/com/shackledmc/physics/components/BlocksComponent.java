/*
 * BlocksComponent.java
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

import net.minecraft.server.v1_6_R2.Material;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.shackledmc.physics.Physics;
import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Experimental.ParticleEffectType;
import com.shackledmc.physics.util.Message;
import com.shackledmc.physics.util.Util;

/**
 * Blocks component.
 * 
 * Handles block-related buffs
 * @author bitWolfy
 *
 */
public class BlocksComponent extends Component implements Listener {
    
    private boolean effects;
    
    public BlocksComponent() {
        super(ComponentType.BLOCKS);
        
        if(!enabled) return;
        
        BlockType.clearCache();
        
        FileConfiguration configFile = Physics.getInstance().getConfig();
        effects = configFile.getBoolean("blocks.effects");
    }
    
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
        Graph componentGraph = metrics.createGraph("component.blocks.enabled");
        
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
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if(!player.hasPermission(type.getPermission())) return;
        
        BlockState blockUnder = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getState();
        if(blockUnder.getType().equals(Material.AIR)) return;
        
        final BlockType type = BlockType.get(blockUnder.getData());
        if(type == null) return;
        
        Vector playerVelocity = player.getVelocity().clone();
        
        switch(type) {
            case SPEED_UP: {
                playerVelocity = playerVelocity.add(player.getLocation().getDirection().multiply(1.01));
                
                if(effects) {
                    Experimental.createEffect(ParticleEffectType.LARGE_SMOKE, "", blockUnder.getLocation(), 0, 0, 5F, 1);
                }
                break;
            }
            
            case SLOW_DOWN: {
                playerVelocity = playerVelocity.subtract(player.getLocation().getDirection().multiply(0.05));
                
                if(effects) {
                    Experimental.createEffect(ParticleEffectType.ENCHANTMENT_TABLE, "", blockUnder.getLocation(), 0, 0, 5F, 1);
                }
                break;
            }
            
            case BOUNCE: {
                playerVelocity = playerVelocity.add(new Vector(0, 0.5 * type.velocity, 0));
                
                if(effects) {
                    Experimental.createEffect(ParticleEffectType.LAVA, "", blockUnder.getLocation(), 0, 0, 5F, 10);
                }
                break;
            }
        }
        
        player.setVelocity(playerVelocity);
    }
    
    private enum BlockType {
        
        SPEED_UP        ("speed-up"),
        SLOW_DOWN       ("slow-down"),
        BOUNCE          ("bounce"),
        ;
        
        private String key;
        
        private boolean enabled;
        private MaterialData data;
        private double velocity;
        
        BlockType(String key) {
            this.key = key;
            refresh();
        }
        
        private void refresh() {
            FileConfiguration configFile = Physics.getInstance().getConfig();
            enabled = configFile.getBoolean("blocks.types." + key + ".enabled");
            String dataString = configFile.getString("blocks.types." + key + ".material");
            data = Util.getBlockMaterial(dataString);
            if(data == null) enabled = false;
            velocity = configFile.getDouble("blocks.types." + key + ".velocity");
        }
        
        private static BlockType get(MaterialData data) {
            for(BlockType type : BlockType.values()) {
                if(type.enabled
                        && type.data.getItemTypeId() == data.getItemTypeId()
                        && type.data.getData() == data.getData()) return type;
            }
            return null;
        }
        
        public static void clearCache() {
            for(BlockType type : BlockType.values()) type.refresh();
        }
    }
    
}
