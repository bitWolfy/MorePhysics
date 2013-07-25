/*
 * MorePhysics.java
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

package com.wolvencraft.morephysics;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.wolvencraft.morephysics.util.Experimental;
import com.wolvencraft.morephysics.util.Message;

public class MorePhysics extends JavaPlugin {
    
    private static MorePhysics instance;
    private static ComponentManager componentManager;
        
    @Override
    public void onEnable() {
        instance = this;
        
        Message.log(
                "+--------------- [ MorePhysics ] ---------------+",
                "| [X] Enabling MorePhysics v." + Message.fillString(this.getDescription().getVersion(), 19) + "|");
        
        if(!new File(getDataFolder(), "config.yml").exists()) {
            Message.log(
                    "|  |- config.yml not found, copying it over     |",
                    "|     for you                                   |");
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        
        Configuration.clearCache();
        componentManager = new ComponentManager();
        
        Message.log(
                "| [X] MorePhysics is enabled                    |",
                "+-----------------------------------------------+"
                );
    }
    
    @Override
    public void onDisable() {
        Message.log("MorePhysics version " + this.getDescription().getVersion() + " is disabled!" );
        
        Configuration.clearCache();
        componentManager.disable();
        
        instance = null;
        componentManager = null;
    }
    
    /**
     * Returns the current instance of the plugin
     * @return MorePhysics instance
     */
    public static MorePhysics getInstance() {
        return instance;
    }
    
    /**
     * Returns the component manager instance
     * @return Component manager instance
     */
    public static ComponentManager getComponentManager() {
        return componentManager;
    }
    
    /**
     * Checks whether the plugin is CraftBukkit compatible
     * @return <b>true</b> if CB versions match, <b>false</b> otherwise
     */
    public static boolean isCraftBukkitCompatible() {
        try { return Experimental.craftBukkitCompatible(); }
        catch(Throwable t) { return false; }
    }
}
