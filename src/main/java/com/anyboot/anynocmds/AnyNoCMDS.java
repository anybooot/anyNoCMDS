package com.anyboot.anynocmds;

import com.anyboot.anynocmds.command.MainCommandExecutor;
import com.anyboot.anynocmds.config.ConfigCache;
import com.anyboot.anynocmds.listeners.PlayerCommandPreProcessListener;
import com.anyboot.anynocmds.listeners.PlayerCommandSendListener;
import com.anyboot.anynocmds.listeners.TabCompleteBlockerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class AnyNoCMDS extends JavaPlugin {

    private static AnyNoCMDS instance;
    private ConfigCache configCache;

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        configCache = new ConfigCache(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerCommandPreProcessListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandSendListener(this), this);
        getServer().getPluginManager().registerEvents(new TabCompleteBlockerListener(this), this);

        // Register main command
        var command = getCommand("anynocmds");
        if (command != null) {
            MainCommandExecutor executor = new MainCommandExecutor(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("anyNoCMDS enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("anyNoCMDS disabled.");
    }

    public static AnyNoCMDS getInstance() {
        return instance;
    }

    public ConfigCache getConfigCache() {
        return configCache;
    }

    public void reloadConfig() {
        configCache.reloadConfig();
    }
}
