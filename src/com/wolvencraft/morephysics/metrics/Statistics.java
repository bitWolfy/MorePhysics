/*
 * Statistics.java
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

package com.wolvencraft.morephysics.metrics;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;

import com.wolvencraft.morephysics.MorePhysics;
import com.wolvencraft.morephysics.util.Message;

/**
 * Statistics collection module
 * 
 * @author bitWolfy
 *
 */
@Getter(AccessLevel.PUBLIC)
public class Statistics {
    
    private PluginMetrics metrics;
    
    public Statistics(MorePhysics plugin) {
        try { metrics = new PluginMetrics(plugin); }
        catch (IOException e) { Message.log("| [X] PluginMetrics has failed to load          |"); }
    }
    
    public void start() {
        if(metrics == null) return;
        metrics.start();
        Message.log("| [X] PluginMetrics has started                 |");
    }
    
}
