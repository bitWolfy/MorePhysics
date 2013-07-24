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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.ComponentManager.PluginComponent;
import com.wolvencraft.morephysics.util.Message;
import com.wolvencraft.morephysics.util.BlockUtil;

public class WeightComponent extends Component implements Listener {
    
    private static Map<MaterialData, Double> weightMap;
    
    private static FileConfiguration weightData = null;
    private static File weightDataFile = null;
    
    public WeightComponent() {
        super(PluginComponent.WEIGHT);
        
        if(!enabled) return;
        
        Bukkit.getServer().getPluginManager().registerEvents(this, MorePhysics.getInstance());
        
        if(!new File(MorePhysics.getInstance().getDataFolder(), "weight.yml").exists()) {
            getWeightData().options().copyDefaults(true);
            saveWeightData();
        }
        
        weightMap = new HashMap<MaterialData, Double>();
        
        for(String raw : getWeightData().getStringList("blocks")) {
            String[] data = raw.split(",");
            if(data.length != 2) continue;
            try { weightMap.put(BlockUtil.getBlockMaterial(data[0]), Double.parseDouble(data[1])); }
            catch (NumberFormatException nfe) { continue; }
            catch (Exception ex) { continue; }
        }
    }
    
    /**
     * Returns the player weight.
     * @param player Player to evaluate
     * @return <b>double</b> weight
     */
    public static double get(Player player) {
        PlayerInventory inventory = player.getInventory();
        double totalWeight = get(inventory);
        for(ItemStack armor : inventory.getArmorContents())
            totalWeight += get(armor);
        return totalWeight;
    }
    
    /**
     * Returns the weight of a player inventory.<br />
     * Armor weight is <b>not</b> included.
     * @param inventory Inventory to evaluate
     * @return <b>double</b> weight
     */
    public static double get(PlayerInventory inventory) {
        double totalWeight = 0;
        ListIterator<ItemStack> it = inventory.iterator();
        while(it.hasNext()) {
            ItemStack curItem = (ItemStack) it.next();
            totalWeight += get(curItem);
            it.remove();
        }
        return totalWeight;
    }
    
    /**
     * Returns the weight of several item stacks.<br />
     * Most often used to measure the weight of player armor.
     * @param items Items to evaluate
     * @return <b>double</b> weight
     */
    public static double get(ItemStack[] items) {
        int totalWeight = 0;
        for(ItemStack item : items) totalWeight += get(item);
        return totalWeight;
    }
    
    /**
     * Returns the weight of an item stack.
     * @param stack Item stack to evaluate
     * @return <b>double</b> weight
     */
    public static double get(ItemStack stack) {
        double blockWeight = get(stack.getData());
        return blockWeight * stack.getAmount();
    }
    
    /**
     * Returns the weight of an item type.
     * @param material Item type to evaluate
     * @return <b>double</b> weight
     */
    public static double get(MaterialData material) {
        return weightMap.get(material).doubleValue();
    }
    
    private static void reloadWeightData() {        
        if (weightDataFile == null) weightDataFile = new File(MorePhysics.getInstance().getDataFolder(), "english.yml");
        weightData = YamlConfiguration.loadConfiguration(weightDataFile);
        
        InputStream defConfigStream = MorePhysics.getInstance().getResource("weight.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            weightData.setDefaults(defConfig);
        }
    }
    
    public static FileConfiguration getWeightData() {
        if (weightData == null) reloadWeightData();
        return weightData;
    }
    
    private static void saveWeightData() {
        if (weightData == null || weightDataFile == null) return;
        try { weightData.save(weightDataFile); }
        catch (IOException ex) { Message.log(Level.SEVERE, "Could not save config to " + weightDataFile); }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission(permission)) return;
        
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if(player.hasPermission(permission)) return;
        
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission(permission)) return;
        
    }
    
}
