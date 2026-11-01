package com.anyboot.anynocmds.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;

public class ConfigCache {

    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;

    // Messages
    public String prefix;
    public String commandDenied;
    public String subcommandDenied;
    public String noPermission;
    public String noSuchSubcommand;
    public String configReloaded;
    public String addedToWhitelist;
    public String removedFromWhitelist;
    public String addedToBlacklist;
    public String removedFromBlacklist;
    public String groupDoesntExist;
    public String invalidCommand;

    // Settings
    public MessageType messageType = MessageType.CHAT;
    public boolean commandDeniedSoundEnabled = false;
    public String commandDeniedSoundKey = "entity.villager.no";
    public float commandDeniedSoundVolume = 1.0f;
    public float commandDeniedSoundPitch = 1.0f;
    public boolean hideUnknownCommands = false;
    public boolean debug = false;

    // Groups
    private final Map<String, CWGroup> groups = new LinkedHashMap<>();

    public ConfigCache(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() {
        // Save default config if not exists
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Load messages
        prefix = config.getString("messages.prefix", "anyNoCMDS > ");
        commandDenied = config.getString("messages.command_denied", "No such command.");
        subcommandDenied = config.getString("messages.subcommand_denied", "You cannot use this subcommand.");
        noPermission = config.getString("messages.no_permission", "<red>You don't have permission to do this.");
        noSuchSubcommand = config.getString("messages.no_such_subcommand", "<red>No subcommand by that name.");
        configReloaded = config.getString("messages.config_reloaded", "<yellow>Configuration reloaded.");
        addedToWhitelist = config.getString("messages.added_to_whitelist", "<yellow>Whitelisted command <gold>%s <yellow>for group <gold>%s");
        removedFromWhitelist = config.getString("messages.removed_from_whitelist", "<yellow>Removed command <gold>%s <yellow>from group <gold>%s");
        addedToBlacklist = config.getString("messages.added_to_blacklist", "<yellow>Blacklisted subcommand <gold>%s <yellow>for group <gold>%s");
        removedFromBlacklist = config.getString("messages.removed_from_blacklist", "<yellow>Removed subcommand <gold>%s <yellow>from group <gold>%s");
        groupDoesntExist = config.getString("messages.group_doesnt_exist", "<red>Group doesn't exist or error occurred.");
        invalidCommand = config.getString("messages.invalid_command", "<red>Invalid command.");

        // Message type
        String messageTypeStr = config.getString("message_type", "CHAT");
        try {
            messageType = MessageType.valueOf(messageTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            messageType = MessageType.CHAT;
        }

        // Sound settings
        commandDeniedSoundEnabled = config.getBoolean("command_denied_sound.enabled", false);
        commandDeniedSoundKey = config.getString("command_denied_sound.key", "entity.villager.no");
        commandDeniedSoundVolume = (float) config.getDouble("command_denied_sound.volume", 1.0);
        commandDeniedSoundPitch = (float) config.getDouble("command_denied_sound.pitch", 1.0);

        // Other settings
        hideUnknownCommands = config.getBoolean("hide_unknown_commands", false);
        debug = config.getBoolean("debug", false);

        // Load groups
        groups.clear();
        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                if (groupSection != null) {
                    List<String> commands = groupSection.getStringList("commands");
                    List<String> subcommands = groupSection.getStringList("subcommands");
                    String customDeniedMessage = groupSection.getString("custom_command_denied_message", "");
                    groups.put(groupName.toLowerCase(), new CWGroup(groupName.toLowerCase(), commands, subcommands, customDeniedMessage));
                }
            }
        }

        // Ensure default group exists
        if (!groups.containsKey("default")) {
            List<String> defaultCommands = Arrays.asList(
                "help", "spawn", "bal", "balance", "baltop", "pay",
                "r", "msg", "tpa", "tpahere", "tpaccept", "tpdeny", "warp"
            );
            List<String> defaultSubcommands = Collections.singletonList("help about");
            groups.put("default", new CWGroup("default", defaultCommands, defaultSubcommands, ""));
        }

        debug("Config loaded with " + groups.size() + " groups");
    }

    public void reloadConfig() {
        loadConfig();
    }

    public CWGroup getGroup(String name) {
        return groups.get(name.toLowerCase());
    }

    public Map<String, CWGroup> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    public Set<String> getGroupNames() {
        return groups.keySet();
    }

    public boolean hasGroup(String name) {
        return groups.containsKey(name.toLowerCase());
    }

    public void debug(String message) {
        if (debug) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void warning(String message) {
        plugin.getLogger().warning(message);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }
}