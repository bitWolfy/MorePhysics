/*
 * WeightComponent.java
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.ComponentType;
import com.wolvencraft.morephysics.util.Message;
import com.wolvencraft.morephysics.util.Util;

/**
 * Weight component.
 * 
 * Handles inventory weight and its impact on player
 * @author bitWolfy
 *
 */
public class WeightComponent extends Component implements Listener {
    
    private Map<MaterialData, Double> weightMap;
    
    private double defaultWeight;
    private double speedMultiplyer;
    private boolean exemptCreative;
    
    private FileConfiguration weightData = null;
    private File weightDataFile = null;
    
    public WeightComponent() {
        super(ComponentType.WEIGHT);
        
        if(!enabled) return;
        
        defaultWeight = getWeightData().getDouble("general.default");
        
        FileConfiguration configFile = MorePhysics.getInstance().getConfig();
        speedMultiplyer = configFile.getDouble("weight.multiplyer");
        exemptCreative = configFile.getBoolean("weight.exempt-creative");
    }
    
    @Override
    public void onEnable() {
        if(!new File(MorePhysics.getInstance().getDataFolder(), "weight.yml").exists()) {
            Message.log(
                    "|  |- weight.yml not found, copying it over     |",
                    "|     for you                                   |"
                    );
            getWeightData().options().copyDefaults(true);
            saveWeightData();
        }
        
        weightMap = new HashMap<MaterialData, Double>();
        
        for(String raw : getWeightData().getStringList("blocks")) {
            String[] data = raw.split(",");
            if(data.length != 2) continue;
            try { weightMap.put(Util.getBlockMaterial(data[0]), Double.parseDouble(data[1])); }
            catch (NumberFormatException nfe) { continue; }
            catch (Exception ex) { continue; }
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
    }
    
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
    
    /**
     * Processes player weight
     * @param player Player to process
     */
    public double getPlayerWeight(Player player) {
        PlayerInventory inventory = player.getInventory();
        
        double totalWeight = 0;
        ListIterator<ItemStack> it = inventory.iterator();
        while(it.hasNext()) {
            ItemStack curItem = (ItemStack) it.next();
            if(curItem == null) continue;
            totalWeight += getStackWeight(curItem);
        }
        
        for(ItemStack armor : inventory.getArmorContents()) totalWeight += getStackWeight(armor);
        
        player.setMetadata("weight", new FixedMetadataValue(MorePhysics.getInstance(), totalWeight));
        
        return totalWeight;
    }
    
    /**
     * Returns the weight of an item stack.
     * @param stack Item stack to evaluate
     * @return <b>double</b> weight
     */
    private double getStackWeight(ItemStack stack) {
        double blockWeight = getMaterialWeight(stack.getData());
        return blockWeight * stack.getAmount();
    }
    
    /**
     * Returns the weight of an item type.
     * @param material Item type to evaluate
     * @return <b>double</b> weight
     */
    private double getMaterialWeight(MaterialData material) {
        if(weightMap.containsKey(material)) return weightMap.get(material).doubleValue();
        else return defaultWeight;
    }
    
    /**
     * Reloads the weight configuration from file
     */
    private void reloadWeightData() {        
        if (weightDataFile == null) weightDataFile = new File(MorePhysics.getInstance().getDataFolder(), "weight.yml");
        weightData = YamlConfiguration.loadConfiguration(weightDataFile);
        
        InputStream defConfigStream = MorePhysics.getInstance().getResource("weight.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            weightData.setDefaults(defConfig);
        }
    }
    
    /**
     * Returns the weight configuration file
     * @return Weight configuration file
     */
    private FileConfiguration getWeightData() {
        if (weightData == null) reloadWeightData();
        return weightData;
    }
    
    /**
     * Saves the weight configuration to file
     */
    private void saveWeightData() {
        if (weightData == null || weightDataFile == null) return;
        try { weightData.save(weightDataFile); }
        catch (IOException ex) { Message.log(Level.SEVERE, "Could not save config to " + weightDataFile); }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission(permission)) return;
        
        setPlayerSpeed(player, getPlayerWeight(player));
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if(!player.hasPermission(permission)
                || (exemptCreative
                        && player.getGameMode().equals(GameMode.CREATIVE))) return;
        
        double weight = getPlayerWeight(player);
        setPlayerSpeed(player, weight);
        Message.debug(
                "Player weight = " + weight,
                "Setting speed to " + player.getWalkSpeed()
                );
    }
    
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = (Player) event.getPlayer();
        if(!player.hasPermission(permission)
                || (exemptCreative
                        && player.getGameMode().equals(GameMode.CREATIVE))) return;
        
        setPlayerSpeed(player, getPlayerWeight(player));
        
        double weight = getPlayerWeight(player);
        setPlayerSpeed(player, weight);
        Message.debug(
                "Player weight = " + weight,
                "Setting speed to " + player.getWalkSpeed()
                );
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = (Player) event.getPlayer();
        if(!player.hasPermission(permission)
                || (exemptCreative
                        && player.getGameMode().equals(GameMode.CREATIVE))) return;
        
      setPlayerSpeed(player, getPlayerWeight(player));
      
      double weight = getPlayerWeight(player);
      setPlayerSpeed(player, weight);
      Message.debug(
              "Player weight = " + weight,
              "Setting speed to " + player.getWalkSpeed()
              );
    }
    
    /**
     * Returns the default player speed on this server
     * @return Default player speed
     */
    public float getDefaultPlayerSpeed() {
        return 0.2f;
    }
    
    /**
     * Sets the player speed
     * @param player Player to process
     * @param weight Inventory weight
     */
    public void setPlayerSpeed(Player player, double weight) {
        player.setWalkSpeed(getDefaultPlayerSpeed());
        
        float speed = getDefaultPlayerSpeed() - (float) (weight * this.speedMultiplyer);
        if(speed <= 0) speed = 0.01f;
        else if(speed > 1) speed = 1f;
        
        player.setWalkSpeed(speed);
    }
    
}
