/*
 * MaterialHook.java
 * 
 * MorePhysics
 * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
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

package com.wolvencraft.morephysics.util;

import java.util.ArrayList;
import java.util.List;

public enum MaterialHook {
    CAKE_BLOCK ("cake_block", "cake_block", "cake"),
    COAL_ORE ("coal_ore", "coalore", "coal"),
    DIAMOND_BLOCK ("diamond_block", "diamondblock"),
    DIAMOND_ORE ("diamond_ore", "diamondore", "diamond"),
    EMERALD_BLOCK ("emerald_block", "emeraldblock"),
    EMERALD_ORE ("emerald_ore", "emeraldore", "emerald"),
    GOLD_BLOCK ("gold_block", "goldblock"),
    GOLD_ORE ("gold_ore", "goldore", "gold"),
    IRON_BLOCK ("iron_block", "ironblock"),
    IRON_ORE ("iron_ore", "ironore", "iron"),
    LAPIS_ORE ("lapis_ore", "lapisore", "lapislazuli", "lapis"),
    LAPIS_BLOCK ("lapis_block", "lapisblock"),
    MELON_BLOCK ("melon_block", "pumpkinblock", "melon"),
    NOTE_BLOCK ("note_block", "noteblock"),
    SNOW_BLOCK ("snow_block", "snowblock", "snow"),
    SUGARCANE_BLOCK ("sugarcane_block", "sugarcaneblock", "sugarcane"),
    PUMPKIN_BLOCK ("pumpkin_block", "pumpkinblock", "pumpkin"),
    QUARTZ_ORE ("quartz_ore", "quartzore", "quartz"),
    QUARTZ_BLOCK ("quartz_block", "quartzblock"),
    REDSTONE_ORE ("redstone_ore", "redstoneore", "redstone"),
    REDSTONE_BLOCK ("redstone_block", "redstoneblock");
    
    private List<String> alias;
    
    private MaterialHook(String... alias) {
        this.alias = new ArrayList<String>();
        for(String i : alias) { this.alias.add(i); }
    }
    
    public String getMaterial() { return alias.get(0); }
    public List<String> getAlias() { return alias; }
    
    public static MaterialHook match(String name) {
        MaterialHook ores[] = MaterialHook.class.getEnumConstants();
        for(MaterialHook ore : ores) {
            if(ore.getAlias().indexOf(name) != -1) return ore;
        }
        return null;
    }
}
