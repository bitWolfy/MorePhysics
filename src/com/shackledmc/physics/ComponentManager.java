/*
 * ComponentManager.java
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

package com.shackledmc.physics;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.shackledmc.physics.components.ArrowComponent;
import com.shackledmc.physics.components.BlocksComponent;
import com.shackledmc.physics.components.BloodComponent;
import com.shackledmc.physics.components.BoatComponent;
import com.shackledmc.physics.components.Component;
import com.shackledmc.physics.components.MinecartComponent;
import com.shackledmc.physics.components.PistonComponent;
import com.shackledmc.physics.components.PlayerComponent;
import com.shackledmc.physics.components.WeightComponent;
import com.shackledmc.physics.metrics.PluginMetrics;
import com.shackledmc.physics.util.ExceptionHandler;
import com.shackledmc.physics.util.Message;

/**
 * Handles components that are enabled during plugin startup
 * @author bitWolfy
 *
 */
public class ComponentManager {
    
    private List<Component> components;
    
    public ComponentManager() {
        components = new ArrayList<Component>();
    }
    
    /**
     * Enables the components
     */
    public void enable() {
        PluginMetrics metrics = Physics.getStatistics().getMetrics();
        
        for(ComponentType component : ComponentType.values()) {
            try { components.add(component.component.newInstance()); }
            catch(Throwable t) {
                ExceptionHandler.handle(t);
                continue;
            }
        }
        
        for(Component component : components) {
            if(component.isEnabled()) {
                Message.log("| [X] " + Message.fillString(component.getClass().getSimpleName() + " is enabled", 42) + "|");
                component.enable();
            } else
                Message.log("| [X] " + Message.fillString(component.getClass().getSimpleName() + " is disabled", 42) + "|");
            
            if(metrics != null) component.statsInit(metrics);
        }
    }
    
    /**
     * Disables all components
     */
    public void disable() {
        for(Component component : components) {
            component.disable();
        }
        
        components.clear();
    }
    
    /**
     * Returns the component of the specified type
     * @param type Component type
     * @return Requested component, or <b>null</b> if it does not exist
     */
    public Component getComponent(ComponentType type) {
        for(Component component : components) {
            if(component.getType() == type) return component;
        }
        return null;
    }
    
    /**
     * Checks if the component is enabled
     * @param type Component type
     * @return <b>true</b> if the component is enabled, <b>false</b> otherwise
     */
    public boolean isComponentEnabled(ComponentType type) {
        Component component = getComponent(type);
        return component != null && component.isEnabled();
    }
    
    @AllArgsConstructor(access=AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    public enum ComponentType {
        
        ARROW           ("component.arrow.enabled", "arrows", ArrowComponent.class, "physics.arrows"),
        BLOCKS          ("component.blocks.enabled", "blocks", BlocksComponent.class, "physics.blocks"),
        BLOOD           ("component.blood.enabled", "blood", BloodComponent.class, "physics.blood"),
        BOAT            ("component.boat.enabled", "boats", BoatComponent.class, "physics.boats"),
        MINECART        ("component.minecart.enabled", "minecarts", MinecartComponent.class, "physics.minecarts"),
        PISTON          ("component.pistons.enabled", "pistons", PistonComponent.class, "physics.pistons"),
        PLAYER          ("component.player.enabled", "player", PlayerComponent.class, "physics.player"),
        WEIGHT          ("component.weight.enabled", "weight", WeightComponent.class, "physics.weight"),
        ;
        
        private String statsKey;
        private String configKey;
        private Class<? extends Component> component;
        private String permission;
        
    }
    
}
