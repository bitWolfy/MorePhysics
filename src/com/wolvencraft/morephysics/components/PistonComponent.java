/*
 * PistonComponent.java
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.PluginComponent;

/**
 * Piston component.
 * 
 * Handles pistons pushing players and mobs
 * @author bitWolfy
 *
 */
public class PistonComponent extends Component implements Listener {
    
    private boolean calculatePlayerWeight;
    
    public PistonComponent() {
        super(PluginComponent.PISTON);
        
        if(!enabled) return;
        
        LaunchPower.clearCache();
        calculatePlayerWeight = MorePhysics.getInstance().getConfig().getBoolean("pistons.calculate-player-weight");
        
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blocksPushedHandler(BlockPistonExtendEvent event) {
        if(event.getBlocks().isEmpty() || LaunchPower.BLOCKS.power == 0.0) return;
        
        List<Block> pushedBlocks = new LinkedList<Block>(event.getBlocks());
        Collections.reverse(pushedBlocks);
        BlockFace direction = event.getDirection();
        
        double pushDistance = LaunchPower.BLOCKS.power;
        if(direction.equals(BlockFace.UP)) pushDistance *= 0.75;
        else if(direction.equals(BlockFace.DOWN)) pushDistance *= 1.50;
        
        int i = 0;
        for(Block block : pushedBlocks) {
            if(!block.getType().equals(Material.SAND) && !block.getType().equals(Material.GRAVEL)) break;
            new LaunchedBlock(block, direction, pushDistance, i);
            i++;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entitiesPushedHandler(BlockPistonExtendEvent event) {
        if(LaunchPower.ENTITIES.power == 0.0) return;
        
        BlockFace direction = event.getDirection();
        Block pushedBlock = event.getBlock().getRelative(event.getDirection());
        Vector velocity = new Vector(direction.getModX(), direction.getModY(), direction.getModZ());
        velocity.multiply(LaunchPower.ENTITIES.power);
        
        for(Entity pushedEntity : event.getBlock().getChunk().getEntities()) {
            if(!isEntityNearby(pushedEntity, pushedBlock.getLocation())) continue;
            
            Vector entityVelocity = pushedEntity.getVelocity().clone();
            
            if(calculatePlayerWeight && pushedEntity instanceof Player) {
                // TODO Calculate the player weight
                pushedEntity.setVelocity(entityVelocity.add(velocity));
            } else {
                pushedEntity.setVelocity(entityVelocity.add(velocity));
            }
        }
    }
    
    /**
     * Destroys a ghost entity that may sometimes appear
     * @param block Original block
     * @param entity Entity
     * @param add Vector to add
     */
    public void destroyGhostEntity(Block block, Entity entity, Vector add) {
        for(Player player : block.getWorld().getPlayers()) {
            if(player.getLocation().distance(block.getLocation()) >= 50) continue;
            player.sendBlockChange(block.getLocation(), 0, (byte) 0);
            player.sendBlockChange(block.getLocation().add(add), 0, (byte) 0);
        }
    }
    
    /**
     * Checks if the entity is roughly near the location.<br />
     * Near is defined as 1 block away from the location.
     * @param entity Entity to check
     * @param location Location to check
     * @return <b>true</b> if the entity is near the location, <b>false</b> otherwise
     */
    public boolean isEntityNearby(Entity entity, Location location) {
        Location entityLoc = entity.getLocation();
        return entityLoc.distanceSquared(location) <= 1
                || entityLoc.clone().add(0, 1, 0).distanceSquared(location) <= 1;
    }
    
    public static class LaunchedBlock implements Runnable {
        
        private BukkitTask task;
        private Block block;
        private BlockFace direction;
        private double blocks;
        
        public LaunchedBlock(Block block, BlockFace direction, double blocks, int delay) {
            this.block = block;
            this.blocks = blocks;
            this.direction = direction;
            task = Bukkit.getScheduler().runTaskTimer(MorePhysics.getInstance(), this, delay, 1L);
        }
        
        @Override
        public void run() {
            if(blocks <= 0 || !block.getRelative(direction).getType().equals(Material.AIR)) {
                task.cancel();
                return;
            }
            block.getRelative(direction).setTypeIdAndData(block.getTypeId(), block.getData(), true);
            block.setType(Material.AIR);
            block = block.getRelative(direction);
            blocks--;
        }
        
    }
    
    private enum LaunchPower {
        
        ENTITIES        ("entities"),
        BLOCKS          ("blocks"),
        ;
        
        private String key;
        private double power;
        
        LaunchPower(String key) {
            this.key = key;
            refresh();
        }
        
        private void refresh() {
            power = MorePhysics.getInstance().getConfig().getDouble("pistons.power." + key);
        }
        
        public static void clearCache() {
            for(LaunchPower area : LaunchPower.values()) area.refresh();
        }
        
    }
    
}
