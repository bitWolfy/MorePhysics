/*
 * UpdateChecker.java
 * 
 * UpdateChecker
 * Copyright (C) 2013 bitWolfy
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

package com.shackledmc.physics.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.plugin.Plugin;

/**
 * Checks for a new plugin version at the download site
 * @author bitWolfy
 *
 */
public class UpdateChecker {
    
    private static final String UPDATE_SERVER = "http://dl.shackledmc.com/api/MorePhysics/";
    private static final String CONFIG_KEY = "updater";
    
    private boolean enabled;
    private UpdateChannel channel;
    
    private Plugin plugin;
    
    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
        
        FileConfiguration configFile = plugin.getConfig();
        
        enabled = configFile.getBoolean(CONFIG_KEY + ".enabled");
        String channelStr = configFile.getString(CONFIG_KEY + ".channel");
        channel = UpdateChannel.get(channelStr);
        if(channel.equals(UpdateChannel.NONE)) enabled = false;
    }
    
    public boolean isUpdateAvailable() throws ConnectionTimeoutException {
        if(!enabled) return false;
        String json;
        try { json = readUrl(UPDATE_SERVER); }
        catch (Throwable t) { throw new ConnectionTimeoutException("Unable to connect to the update server", t); }
        
        try { return new PluginVersion(json).isUpdateAvailable(plugin, channel); }
        catch (Throwable t) { return false; }
    }
    
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read); 

            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }
    
    /**
     * Type of an update channel that is requested by the plugin
     * @author bitWolfy
     *
     */
    private enum UpdateChannel {
        
        RECOMMENDED     ("recommended", "rec", "rb"),
        BETA            ("beta", "bt"),
        DEVELOPMENT     ("development", "dev", "db"),
        NONE            ("none", "null"),
        ;
        
        private List<String> alias;
        
        private UpdateChannel(String... alias) {
            this.alias = new ArrayList<String>();
            for(String str : alias) this.alias.add(str);
        }
        
        private static UpdateChannel get(String alias) {
            for(UpdateChannel channel : UpdateChannel.values()) {
                if(channel.alias.contains(alias)) return channel;
            }
            return UpdateChannel.NONE;
        }
    }
    
    /**
     * Thrown if the plugin is unable to connect to the update server
     * @author bitWolfy
     *
     */
    public static class ConnectionTimeoutException extends Exception {
        private static final long serialVersionUID = 2047566547380069600L;
        
        private ConnectionTimeoutException(String message) {
            super(message);
        }
        
        private ConnectionTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    @Getter(AccessLevel.PUBLIC)
    private class PluginVersion {
        
        private String name;
        
        private Build latest;
        private Build beta;
        private Build recommended;
        
        public PluginVersion(String rawJson) {
            VersionSerializable version = new Gson().fromJson(rawJson, VersionSerializable.class);
            
            name = version.name;
            
            latest = new Build(version.latestVersion, version.latestBuild);
            beta = new Build(version.betaVersion, version.betaBuild);
            recommended = new Build(version.recommendedVersion, version.recommendedBuild);
        }
        
        public boolean isUpdateAvailable(Plugin plugin, UpdateChannel updateChannel) {
            Version current = new Version(plugin.getDescription().getVersion());
            switch(updateChannel) {
                case RECOMMENDED: return current.isOlderThan(recommended.version);
                case BETA: return current.isOlderThan(beta.version);
                case DEVELOPMENT: return current.isOlderThan(latest.version);
                default: return false;
            }
        }
        
    }

    @AllArgsConstructor(access=AccessLevel.PUBLIC)
    private static class VersionSerializable {
        
        private String name;
        
        private String latestVersion;
        private String betaVersion;
        private String recommendedVersion;
        
        private int latestBuild;
        private int betaBuild;
        private int recommendedBuild;
        
    }
    
    @Getter(AccessLevel.PUBLIC)
    private static class Build {
        
        private Version version;
        private int build;
        
        public Build(String version, int build) {
            this.version = new Version(version);
            this.build = build;
        }
        
    }

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private static class Version {
        
        private int major;
        private int minor;
        
        public Version(String version) {
            String[] parts = version.split("\\.");
            
            try { major = Integer.parseInt(parts[0]); }
            catch(Throwable t) { major = 0; }
            
            try { minor = Integer.parseInt(parts[1]); }
            catch(Throwable t) { minor = 0; }
        }
        
        public boolean isOlderThan(Version version) {
            return (version.getMajor() > major)
                    || ((version.getMajor() == major) && version.getMinor() > minor);
        }
        
        @Override
        public String toString() {
            return major + "." + minor;
        }
        
    }
}
