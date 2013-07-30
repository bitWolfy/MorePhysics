/*
 * ComponentManager.java
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

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.wolvencraft.morephysics.components.ArrowComponent;
import com.wolvencraft.morephysics.components.BloodComponent;
import com.wolvencraft.morephysics.components.BoatComponent;
import com.wolvencraft.morephysics.components.Component;
import com.wolvencraft.morephysics.components.MinecartComponent;
import com.wolvencraft.morephysics.components.PistonComponent;
import com.wolvencraft.morephysics.components.PlayerComponent;
import com.wolvencraft.morephysics.components.WeightComponent;
import com.wolvencraft.morephysics.metrics.PluginMetrics;
import com.wolvencraft.morephysics.util.ExceptionHandler;
import com.wolvencraft.morephysics.util.Message;

/**
 * Handles components that are enabled during plugin startup
 * @author bitWolfy
 *
 */
public class ComponentManager {
    
    private List<Component> components;
    
    public ComponentManager() {
        components = new ArrayList<Component>();
        PluginMetrics metrics = MorePhysics.getStatistics().getMetrics();
        
        for(ComponentType component : ComponentType.values()) {
            try {
                Component componentObj = component.component.newInstance();
                if(componentObj.isEnabled()) {
                    Message.log("| [X] " + Message.fillString(component.component.getSimpleName() + " is enabled", 42) + "|");
                    componentObj.enable();
                } else
                    Message.log("| [X] " + Message.fillString(component.component.getSimpleName() + " is not enabled", 42) + "|");
                
                if(metrics != null) componentObj.statsInit(metrics);
                components.add(componentObj);
            } catch(Throwable t) {
                ExceptionHandler.handle(t);
                continue;
            }
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
    
    @AllArgsConstructor(access=AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    public enum ComponentType {
        
        ARROW           ("arrows", ArrowComponent.class, "morephysics.arrows"),
        BLOOD           ("blood", BloodComponent.class, "morephysics.blood"),
        BOAT            ("boats", BoatComponent.class, "morephysics.boats"),
        MINECART        ("minecarts", MinecartComponent.class, "morephysics.minecarts"),
        PISTON          ("pistons", PistonComponent.class, "morephysics.pistons"),
        PLAYER          ("player", PlayerComponent.class, "morephysics.player"),
        WEIGHT          ("weight", WeightComponent.class, "morephysics.weight"),
        ;

        private String configKey;
        private Class<? extends Component> component;
        private String permission;
        
    }
    
}
