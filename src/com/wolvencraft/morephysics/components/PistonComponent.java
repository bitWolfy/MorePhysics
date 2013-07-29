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
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.wolvencraft.morephysics.Configuration;
import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.ComponentType;
import com.wolvencraft.morephysics.util.Experimental;
import com.wolvencraft.morephysics.util.Message;
import com.wolvencraft.morephysics.util.Experimental.ParticleEffectType;

/**
 * Piston component.
 * 
 * Handles pistons pushing players and mobs
 * @author bitWolfy
 *
 */
public class PistonComponent extends Component implements Listener {
    
    private static final BlockFace[] DIRECTIONS = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
    
    private boolean calculatePlayerWeight;
    private double weightModifier;
    
    private boolean signControlled;
    private boolean effects;
    
    public PistonComponent() {
        super(ComponentType.PISTON);
        
        if(!enabled) return;
        
        LaunchPower.clearCache();
        
        FileConfiguration configFile = MorePhysics.getInstance().getConfig();
        calculatePlayerWeight = configFile.getBoolean("pistons.weight.enabled");
        weightModifier = configFile.getDouble("pistons.weight.modifier");
        effects = configFile.getBoolean("pistons.effects");
        effects = configFile.getBoolean("pistons.sign-controlled");
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
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blocksPushedHandler(BlockPistonExtendEvent event) {
        if(exemptWorlds.contains(event.getBlock().getWorld().getName())) return;
        
        if(event.getBlocks().isEmpty() || LaunchPower.BLOCKS.power == 0.0) return;
        
        if(signControlled && !checkForSign(event.getBlock())) return;
        
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
        
        if(effects) Experimental.createEffect(ParticleEffectType.EXPLODE, "", event.getBlock().getLocation(), 1f, 1f, 1f, 20);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entitiesPushedHandler(BlockPistonExtendEvent event) {
        if(exemptWorlds.contains(event.getBlock().getWorld().getName())) return;
        
        if(LaunchPower.ENTITIES.power == 0.0) return;

        if(signControlled && !checkForSign(event.getBlock())) return;
        
        BlockFace direction = event.getDirection();
        Block pushedBlock = event.getBlock().getRelative(event.getDirection());
        Vector velocity = new Vector(direction.getModX(), direction.getModY(), direction.getModZ());
        velocity.multiply(LaunchPower.ENTITIES.power);
        
        for(Entity pushedEntity : event.getBlock().getChunk().getEntities()) {
            if(!isEntityNearby(pushedEntity, pushedBlock.getLocation())) continue;
            
            Vector entityVelocity = pushedEntity.getVelocity().clone();
            
            if(pushedEntity instanceof Player) {
                if(!((Player) pushedEntity).hasPermission(type.getPermission())) continue;
                
                if(calculatePlayerWeight && pushedEntity.hasMetadata("weight")) {
                    double weight = pushedEntity.getMetadata("weight").get(0).asDouble();
                    velocity.subtract(velocity.clone().multiply(weight * weightModifier));
                }
                
                pushedEntity.setVelocity(entityVelocity.add(velocity));
            } else {
                pushedEntity.setVelocity(entityVelocity.add(velocity));
            }
        }
        
        if(effects) Experimental.createEffect(ParticleEffectType.EXPLODE, "", event.getBlock().getLocation(), 1f, 1f, 1f, 20);
    }
    
    /**
     * Destroys a ghost entity that may sometimes appear
     * @param block Original block
     * @param entity Entity
     * @param add Vector to add
     */
    protected void destroyGhostEntity(Block block, Entity entity, Vector add) {
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
    private boolean isEntityNearby(Entity entity, Location location) {
        Location entityLoc = entity.getLocation();
        return entityLoc.distanceSquared(location) <= 1
                || entityLoc.clone().add(0, 1, 0).distanceSquared(location) <= 1;
    }
    
    /**
     * Checks for a controlling sign around the block
     * @param block Block to check
     * @return <b>true</b> if there is a valid sign, <b>false</b> otherwise
     */
    private boolean checkForSign(Block block) {
        for(BlockFace direction : DIRECTIONS) {
            Block relBlock = block.getRelative(direction);
            if(!(relBlock instanceof Sign)) continue;
            Sign sign = (Sign) relBlock;
            if(sign.getLine(0).equalsIgnoreCase(Configuration.Prefix.toString())
                    && sign.getLine(1).equalsIgnoreCase("piston")) return true;
        }
        return false;
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
