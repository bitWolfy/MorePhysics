/*
 * Experimental.java
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

package com.shackledmc.physics.util;

import java.lang.reflect.Field;

import net.minecraft.server.v1_6_R2.Packet61WorldEvent;
import net.minecraft.server.v1_6_R2.Packet63WorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class Experimental {
    
    public static void createBlockEffect(Location location, int blockId) {
        Packet61WorldEvent packet = new Packet61WorldEvent(2001,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(), blockId, false);
        ((CraftServer) Bukkit.getServer()).getHandle().sendAll(packet);
    }
    
    public static void createEffect(ParticleEffectType effectType, String extraData, Location location, float xzOffset, float yOffset, float effectSpeed, int particleCount) {
        Packet63WorldParticles sPacket = new Packet63WorldParticles();
        for (Field field : sPacket.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                
                if(fieldName.equals("a")) field.set(sPacket, effectType.alias + extraData);
                else if(fieldName.equals("b")) field.setFloat(sPacket, (float) location.getX());
                else if(fieldName.equals("c")) field.setFloat(sPacket, (float) location.getY());
                else if(fieldName.equals("d")) field.setFloat(sPacket, (float) location.getZ());
                else if(fieldName.equals("e")) field.setFloat(sPacket, xzOffset);
                else if(fieldName.equals("f")) field.setFloat(sPacket, yOffset);
                else if(fieldName.equals("g")) field.setFloat(sPacket, xzOffset);
                else if(fieldName.equals("h")) field.setFloat(sPacket, effectSpeed);
                else if(fieldName.equals("i")) field.setInt(sPacket, particleCount);
            } catch (Exception e) { }
        }
        ((CraftServer) Bukkit.getServer()).getHandle().sendAll(sPacket);
    }
    
    @AllArgsConstructor(access=AccessLevel.PRIVATE)
    public enum ParticleEffectType {
        
        HUGE_EXPLOSION          ("hugeexplosion"),
        LARGE_EXPLOSION         ("largeexplode"),
        FIREWORK_SPARK          ("fireworksSpark"),
        BUBBLE                  ("bubble"),
        SUSPENDED               ("suspended"),
        DEATH_SUSPENDED         ("depthsuspend"),
        TOWN_AURA               ("townaura"),
        CRIT                    ("crit"),
        MAGIC_CRIT              ("magicCrit"),
        SMOKE                   ("smoke"),
        MOB_SPELL               ("mobSpell"),
        MOB_SPELL_AMBIENT       ("mobSpellAmbient"),
        SPELL                   ("spell"),
        INSTANT_SPELL           ("instantSpell"),
        WITCH_MAGIC             ("witchMagic"),
        NOTE                    ("note"),
        PORTAL                  ("portal"),
        ENCHANTMENT_TABLE       ("enchantmenttable"),
        EXPLODE                 ("explode"),
        FLAME                   ("flame"),
        LAVA                    ("lava"),
        FOOTSTEP                ("footstep"),
        SPLASH                  ("splash"),
        LARGE_SMOKE             ("largesmoke"),
        CLOUD                   ("cloud"),
        RED_DUST                ("reddust"),
        SNOWBALL                ("snowballpoof"),
        DRIP_WATER              ("dripWater"),
        DRIP_LAVA               ("dripLava"),
        SNOW_SHOVEL             ("snowshovel"),
        SLIME                   ("slime"),
        HEART                   ("heart"),
        ANGRY_VILLAGER          ("angryVillager"),
        HAPPY_VILLAGER          ("happyVillager"),
        ICON_CRACK              ("iconcrack_"),         // iconcrack_*
        TILE_CRACK              ("tilecrack_"),         // tilecrack_*_*
        ;
        
        private String alias;
        
    }
    
    /**
     * Throws an exception if the class is not CraftBukkit compatible, returns <b>true</b> otherwise
     * @return <b>true</b> if the class is CraftBukkit compatible
     */
    public static boolean craftBukkitCompatible() {
        return true;
    }
    
}
