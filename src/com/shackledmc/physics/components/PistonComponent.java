/*
 * PistonComponent.java
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;

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

import com.shackledmc.physics.Configuration;
import com.shackledmc.physics.Physics;
import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.api.PistonBlockLaunchEvent;
import com.shackledmc.physics.api.PistonEntityLaunchEvent;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;
import com.shackledmc.physics.util.Experimental;
import com.shackledmc.physics.util.Message;
import com.shackledmc.physics.util.Experimental.ParticleEffectType;

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
        
        FileConfiguration configFile = Physics.getInstance().getConfig();
        calculatePlayerWeight = configFile.getBoolean("pistons.weight.enabled");
        weightModifier = configFile.getDouble("pistons.weight.modifier") * WeightComponent.SPEED_MODIFIER_RATIO;
        effects = configFile.getBoolean("pistons.effects");
        signControlled = configFile.getBoolean("pistons.sign-controlled");
    }
    
    @Override
    public void onEnable() {
        if(effects && !Physics.isCraftBukkitCompatible()) {
            Message.log(
                    "|  |- Particle effects are not compatible with  |",
                    "|  |  your CraftBukkit version. Disabling...    |"
                    );
            effects = false;
        }
        
        if(calculatePlayerWeight
                && !Physics.getComponentManager().isComponentEnabled(ComponentType.WEIGHT)) {
            Message.log(
                    "|  |- WeightComponent is disabled - cannot      |",
                    "|  |  calculate player weight into velocity     |"
                    );
            calculatePlayerWeight = false;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, Physics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    
    @Override
    public void statsInit(PluginMetrics metrics) {
        Graph componentGraph = metrics.createGraph("component.piston.enabled");
        
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
    public void blocksPushedHandler(BlockPistonExtendEvent event) {
        if(exemptWorlds.contains(event.getBlock().getWorld().getName())) return;
        
        if(event.getBlocks().isEmpty() || LaunchPower.BLOCKS.power == 0.0) return;
        
        double power = LaunchPower.BLOCKS.power;
        if(signControlled) {
            double overridePower = -1;
            try { overridePower = getControllingSign(event.getBlock()); }
            catch (NoSignFoundException ex) { return; }
            if(overridePower != -1) power = overridePower;
        }
        
        List<Block> pushedBlocks = new LinkedList<Block>(event.getBlocks());
        Collections.reverse(pushedBlocks);
        BlockFace direction = event.getDirection();
        
        if(direction.equals(BlockFace.UP)) power *= 0.75;
        else if(direction.equals(BlockFace.DOWN)) power *= 1.50;
        
        int i = 0;
        for(Block block : pushedBlocks) {
            if(!block.getType().equals(Material.SAND) && !block.getType().equals(Material.GRAVEL)) break;
            PistonBlockLaunchEvent apiEvent = new PistonBlockLaunchEvent(
                    event.getBlock(),
                    new LaunchedBlock(block, direction, power, i));
            Bukkit.getPluginManager().callEvent(apiEvent);
            i++;
        }
        
        if(effects) Experimental.createEffect(ParticleEffectType.EXPLODE, "", event.getBlock().getLocation(), 1f, 1f, 1f, 20);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entitiesPushedHandler(BlockPistonExtendEvent event) {
        if(exemptWorlds.contains(event.getBlock().getWorld().getName())) return;
        
        if(LaunchPower.ENTITIES.power == 0.0) return;

        double power = LaunchPower.ENTITIES.power;
        if(signControlled) {
            double overridePower = -1;
            try { overridePower = getControllingSign(event.getBlock()); }
            catch (NoSignFoundException ex) { return; }
            if(overridePower != -1) power = overridePower;
        }

        BlockFace direction = event.getDirection();
        Block pushedBlock = event.getBlock().getRelative(event.getDirection());
        Vector velocity = new Vector(direction.getModX(), direction.getModY(), direction.getModZ());
        velocity.multiply(power);
        
        WeightComponent weightComponent = null;
        if(calculatePlayerWeight)
            weightComponent = (WeightComponent) Physics.getComponentManager().getComponent(ComponentType.WEIGHT);
        
        for(Entity pushedEntity : event.getBlock().getChunk().getEntities()) {
            if(!isEntityNearby(pushedEntity, pushedBlock.getLocation())) continue;
            
            Vector entityVelocity = pushedEntity.getVelocity().clone();
            
            if(pushedEntity instanceof Player) {
                Player player = (Player) pushedEntity;
                if(!player.hasPermission(type.getPermission())) continue;
                
                if(calculatePlayerWeight
                        && player.hasPermission(ComponentType.WEIGHT.getPermission())) {
                    double weight = weightComponent.getPlayerWeight(player);
                    velocity.subtract(velocity.clone().multiply(weight * weightModifier));
                }
            }
            
            entityVelocity = entityVelocity.add(velocity);
            PistonEntityLaunchEvent apiEvent = new PistonEntityLaunchEvent(event.getBlock(), pushedEntity, entityVelocity);
            Bukkit.getPluginManager().callEvent(apiEvent);
            if(apiEvent.isCancelled()) continue;
            
            pushedEntity.setVelocity(entityVelocity);
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
    private double getControllingSign(Block block) throws NoSignFoundException {
        for(BlockFace direction : DIRECTIONS) {
            Block relBlock = block.getRelative(direction);
            if(!(relBlock instanceof Sign)) continue;
            String[] lines = ((Sign) relBlock).getLines();
            if(lines.length > 2 || lines.length < 1) continue;
            if(!lines[0].equalsIgnoreCase(Configuration.Prefix.toString())) continue;
            if(lines.length == 2) {
                try { return Double.parseDouble(lines[1]); }
                catch (Throwable t) { return -1; }
            } else return -1;
        }
        throw new NoSignFoundException("No applicable sign found");
    }
    
    @Getter(AccessLevel.PUBLIC)
    public static class LaunchedBlock implements Runnable {
        
        private BukkitTask task;
        private Block block;
        private BlockFace direction;
        private double blocks;
        
        public LaunchedBlock(Block block, BlockFace direction, double blocks, int delay) {
            this.block = block;
            this.blocks = blocks;
            this.direction = direction;
            task = Bukkit.getScheduler().runTaskTimer(Physics.getInstance(), this, delay, 1L);
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
            power = Physics.getInstance().getConfig().getDouble("pistons.power." + key);
        }
        
        public static void clearCache() {
            for(LaunchPower area : LaunchPower.values()) area.refresh();
        }
        
    }
    
    private static class NoSignFoundException extends Exception {
        
        private static final long serialVersionUID = -4897081746992527847L;

        public NoSignFoundException(String message) {
            super(message);
        }
        
    }
    
}
