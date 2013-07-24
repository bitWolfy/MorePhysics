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
import com.wolvencraft.morephysics.components.BoatComponent;
import com.wolvencraft.morephysics.components.Component;
import com.wolvencraft.morephysics.components.MinecartComponent;
import com.wolvencraft.morephysics.components.PistonComponent;
import com.wolvencraft.morephysics.components.WeightComponent;
import com.wolvencraft.morephysics.util.ExceptionHandler;

public class ComponentManager {
    
    private static List<Component> components = new ArrayList<Component>();
    
    public ComponentManager() {
        components.clear();
        
        for(PluginComponent component : PluginComponent.values()) {
            try {
                Component componentObj = component.component.newInstance();
                components.add(componentObj);
            } catch(Throwable t) {
                ExceptionHandler.handle(t);
                continue;
            }
        }
    }
    
    @AllArgsConstructor(access=AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    public enum PluginComponent {
        
        ARROW           ("arrows", ArrowComponent.class),
        BOAT            ("boats", BoatComponent.class),
        MINECART        ("minecarts", MinecartComponent.class),
        PISTON          ("pistons", PistonComponent.class),
        WEIGHT          ("weight", WeightComponent.class),
        ;

        private String configKey;
        private Class<? extends Component> component;
        
    }
    
}
