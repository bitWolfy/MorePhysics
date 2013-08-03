/*
 * Component.java
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

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.configuration.file.FileConfiguration;

import com.shackledmc.physics.ComponentManager.ComponentType;
import com.shackledmc.physics.Physics;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.metrics.PluginMetrics.Graph;

@Getter(AccessLevel.PUBLIC)
public abstract class Component {
    
    protected ComponentType type;
    
    protected boolean enabled;
    protected List<String> exemptWorlds;
    
    public Component(ComponentType type) {
        this.type = type;
        
        FileConfiguration configFile = Physics.getInstance().getConfig();
        enabled = configFile.getBoolean(type.getConfigKey() + ".enabled");
        exemptWorlds = configFile.getStringList(type.getConfigKey() + ".exempt-worlds");
    }
    
    /**
     * Component enabling routine
     */
    public final void enable() {
        onEnable();
    }
    
    /**
     * Actions performed when the component is being enabled
     */
    public void onEnable() { }
    
    /**
     * Component disabling routine
     */
    public final void disable() {
        
    }
    
    /**
     * Actions performed when the component is being disabled
     */
    public void onDisable() { }
    
    /**
     * Initializes the plugin statistic
     */
    public final void statsInit(PluginMetrics metrics) {
        Graph statusGraph = metrics.createGraph(type.getStatsKey());
        
        statusGraph.addPlotters(
                new PluginMetrics.Plotter("Enabled") {
                    @Override public int getValue() { return enabled ? 1 : 0; }
                },
                new PluginMetrics.Plotter("Disabled") {
                    @Override public int getValue() { return !enabled ? 1 : 0; }
                }
        );
        
        onStatsInit(metrics);
    }
    
    public void onStatsInit(PluginMetrics metrics) {
        
    }
}
