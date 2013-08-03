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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.Physics;
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
    
    private static final String cancelFallDamageMetaKey = "physics.component.blocks.cancelFallDamage";
    
    private boolean affectVehicles;
    private boolean cancelFallDamage;
    
    private boolean effects;
    
    public BlocksComponent() {
        super(ComponentType.BLOCKS);
        
        if(!enabled) return;
        
        BlockType.clearCache();
        
        FileConfiguration configFile = Physics.getInstance().getConfig();
        effects = configFile.getBoolean("blocks.affect-vehicles");
        cancelFallDamage = configFile.getBoolean("blocks.cancel-fall-damage");
        
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
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if(!player.hasPermission(type.getPermission())) return;
        
        BlockState blockUnder = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getState();
        if(blockUnder.getType().equals(Material.AIR)) return;
        
        BlockType type = BlockType.get(blockUnder.getData());
        if(type == null) return;
        
        processVelocity(player, type, blockUnder.getLocation());
        
        if(!cancelFallDamage) return;
        
        player.setMetadata(cancelFallDamageMetaKey, new FixedMetadataValue(Physics.getInstance(), true));
        Bukkit.getScheduler().runTaskLater(Physics.getInstance(), new Runnable() {
            
            @Override
            public void run() {
                if(player != null && player.hasMetadata(cancelFallDamageMetaKey))
                    player.removeMetadata(cancelFallDamageMetaKey, Physics.getInstance());
            }
            
        }, 60L);
    }
    
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if(!affectVehicles) return;
        
        Vehicle vehicle = event.getVehicle();
        
        BlockState blockUnder = vehicle.getLocation().getBlock().getRelative(BlockFace.DOWN).getState();
        if(blockUnder.getType().equals(Material.AIR)) return;
        
        BlockType type = BlockType.get(blockUnder.getData());
        if(type == null) return;
        
        processVelocity(vehicle, type, blockUnder.getLocation());
        
        if(!cancelFallDamage
                || !(vehicle.getPassenger() instanceof Player)) return;
        
        final Player player = (Player) vehicle.getPassenger();
        
        player.setMetadata(cancelFallDamageMetaKey, new FixedMetadataValue(Physics.getInstance(), true));
        Bukkit.getScheduler().runTaskLater(Physics.getInstance(), new Runnable() {
            
            @Override
            public void run() {
                if(player != null && player.hasMetadata(cancelFallDamageMetaKey))
                    player.removeMetadata(cancelFallDamageMetaKey, Physics.getInstance());
            }
            
        }, 60L);
        
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!cancelFallDamage
                || !event.getCause().equals(DamageCause.FALL)
                || !(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();

        if(!player.hasPermission(type.getPermission())) return;
        
        if(player.hasMetadata(cancelFallDamageMetaKey)) {
            player.removeMetadata(cancelFallDamageMetaKey, Physics.getInstance());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!cancelFallDamage) return;
        
        Player player = event.getPlayer();
        
        if(player.hasMetadata(cancelFallDamageMetaKey)) {
            player.removeMetadata(cancelFallDamageMetaKey, Physics.getInstance());
        }
    }
    
    /**
     * Performs the necessary calculations for the entity velocity
     * @param entity Entity to process
     * @param blockUnder Block type
     * @param blockLocation Block location
     */
    private void processVelocity(Entity entity, BlockType blockUnder, Location blockLocation) {
        Vector velocity = entity.getVelocity().clone();
        
        switch(blockUnder) {
            case SPEED_UP: {
                velocity = velocity.add(entity.getLocation().getDirection().multiply(1.01));
                
                if(effects) {
                    Experimental.createEffect(ParticleEffectType.LARGE_SMOKE, "", blockLocation, 0, 0, 5F, 1);
                }
                break;
            }
            
            case SLOW_DOWN: {
                velocity = velocity.subtract(entity.getLocation().getDirection().multiply(0.05));
                
                if(effects) {
                    Experimental.createEffect(ParticleEffectType.ENCHANTMENT_TABLE, "", blockLocation, 0, 0, 5F, 1);
                }
                break;
            }
            
            case BOUNCE: {
                velocity = velocity.add(new Vector(0, 0.5 * blockUnder.velocity, 0));
                
                if(effects) {
                    Experimental.createEffect(ParticleEffectType.LAVA, "", blockLocation, 0, 0, 5F, 10);
                }
                break;
            }
        }
        
        entity.setVelocity(velocity);
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
