package com.anyboot.anynocmds.command;

import com.anyboot.anynocmds.AnyNoCMDS;
import com.anyboot.anynocmds.config.CommandUtil;
import com.anyboot.anynocmds.config.ConfigCache;
import com.anyboot.anynocmds.config.CWGroup;
import com.anyboot.anynocmds.config.CWPermission;
import com.anyboot.anynocmds.config.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class MainCommandExecutor implements CommandExecutor, TabCompleter {

    private final AnyNoCMDS plugin;

    public MainCommandExecutor(AnyNoCMDS plugin) {
        this.plugin = plugin;
    }

    private Component mm(String raw) {
        return MessageUtil.parse(raw);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "whitelist":
                return handleWhitelist(sender, args);
            case "blacklist":
                return handleBlacklist(sender, args);
            case "list":
                return handleList(sender, args);
            case "dump":
                return handleDump(sender);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().invalidCommand));
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!(sender instanceof Player) || sender.hasPermission(CWPermission.RELOAD.permission())) {
            plugin.reloadConfig();
            sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().configReloaded));
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.updateCommands();
            }
            return true;
        }
        sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().noPermission));
        return true;
    }

    private boolean handleWhitelist(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission(CWPermission.WHITELIST.permission())) {
            if (args.length < 3) {
                sender.sendMessage(mm(plugin.getConfigCache().prefix + "<red>Usage: /anynocmds whitelist <group> <add|remove> <command>"));
                return true;
            }

            String groupName = args[1].toLowerCase();
            String action = args[2].toLowerCase();
            String commandName = args.length > 3 ? args[3].toLowerCase() : "";

            ConfigCache configCache = plugin.getConfigCache();
            CWGroup group = configCache.getGroup(groupName);

            if (group == null) {
                sender.sendMessage(mm(configCache.prefix + configCache.groupDoesntExist));
                return true;
            }

            FileConfiguration config = configCache.getConfig();
            String path = "groups." + groupName + ".commands";
            List<String> commands = config.getStringList(path);

            if (action.equals("add")) {
                if (commandName.isEmpty()) {
                    sender.sendMessage(mm(configCache.prefix + "<red>Usage: /anynocmds whitelist <group> add <command>"));
                    return true;
                }
                if (!commands.contains(commandName)) {
                    commands.add(commandName);
                    config.set(path, commands);
                    configCache.saveConfig();
                    configCache.loadConfig();
                    String msg = configCache.addedToWhitelist
                            .replaceFirst("%s", commandName)
                            .replaceFirst("%s", groupName);
                    sender.sendMessage(mm(configCache.prefix + msg));
                } else {
                    sender.sendMessage(mm(configCache.prefix + "<red>Command already whitelisted for this group."));
                }
            } else if (action.equals("remove")) {
                if (commandName.isEmpty()) {
                    sender.sendMessage(mm(configCache.prefix + "<red>Usage: /anynocmds whitelist <group> remove <command>"));
                    return true;
                }
                if (commands.remove(commandName)) {
                    config.set(path, commands);
                    configCache.saveConfig();
                    configCache.loadConfig();
                    String msg = configCache.removedFromWhitelist
                            .replaceFirst("%s", commandName)
                            .replaceFirst("%s", groupName);
                    sender.sendMessage(mm(configCache.prefix + msg));
                } else {
                    sender.sendMessage(mm(configCache.prefix + "<red>Command not found in whitelist."));
                }
            } else {
                sender.sendMessage(mm(configCache.prefix + "<red>Usage: /anynocmds whitelist <group> <add|remove> <command>"));
            }
            return true;
        }
        sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().noPermission));
        return true;
    }

    private boolean handleBlacklist(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission(CWPermission.BLACKLIST.permission())) {
            if (args.length < 3) {
                sender.sendMessage(mm(plugin.getConfigCache().prefix + "<red>Usage: /anynocmds blacklist <group> <add|remove> <subcommand>"));
                return true;
            }

            String groupName = args[1].toLowerCase();
            String action = args[2].toLowerCase();
            String subcommand = args.length > 3 ? args[3].toLowerCase() : "";

            ConfigCache configCache = plugin.getConfigCache();
            CWGroup group = configCache.getGroup(groupName);

            if (group == null) {
                sender.sendMessage(mm(configCache.prefix + configCache.groupDoesntExist));
                return true;
            }

            FileConfiguration config = configCache.getConfig();
            String path = "groups." + groupName + ".subcommands";
            List<String> subcommands = config.getStringList(path);

            if (action.equals("add")) {
                if (subcommand.isEmpty()) {
                    sender.sendMessage(mm(configCache.prefix + "<red>Usage: /anynocmds blacklist <group> add <subcommand>"));
                    return true;
                }
                if (!subcommands.contains(subcommand)) {
                    subcommands.add(subcommand);
                    config.set(path, subcommands);
                    configCache.saveConfig();
                    configCache.loadConfig();
                    String msg = configCache.addedToBlacklist
                            .replaceFirst("%s", subcommand)
                            .replaceFirst("%s", groupName);
                    sender.sendMessage(mm(configCache.prefix + msg));
                } else {
                    sender.sendMessage(mm(configCache.prefix + "<red>Subcommand already blacklisted for this group."));
                }
            } else if (action.equals("remove")) {
                if (subcommand.isEmpty()) {
                    sender.sendMessage(mm(configCache.prefix + "<red>Usage: /anynocmds blacklist <group> remove <subcommand>"));
                    return true;
                }
                if (subcommands.remove(subcommand)) {
                    config.set(path, subcommands);
                    configCache.saveConfig();
                    configCache.loadConfig();
                    String msg = configCache.removedFromBlacklist
                            .replaceFirst("%s", subcommand)
                            .replaceFirst("%s", groupName);
                    sender.sendMessage(mm(configCache.prefix + msg));
                } else {
                    sender.sendMessage(mm(configCache.prefix + "<red>Subcommand not found in blacklist."));
                }
            } else {
                sender.sendMessage(mm(configCache.prefix + "<red>Usage: /anynocmds blacklist <group> <add|remove> <subcommand>"));
            }
            return true;
        }
        sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().noPermission));
        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission(CWPermission.LIST.permission())) {
            ConfigCache configCache = plugin.getConfigCache();

            if (args.length > 1) {
                String groupName = args[1].toLowerCase();
                CWGroup group = configCache.getGroup(groupName);
                if (group == null) {
                    sender.sendMessage(mm(configCache.prefix + configCache.groupDoesntExist));
                    return true;
                }

                sender.sendMessage(mm(configCache.prefix + "<gold>Group: <white>" + group.getName()));
                sender.sendMessage(mm(configCache.prefix + "<gold>Commands (whitelist): <white>" + (group.getCommands().isEmpty() ? "<gray>none" : String.join(", ", group.getCommands()))));
                sender.sendMessage(mm(configCache.prefix + "<gold>Subcommands (blacklist): <white>" + (group.getSubcommands().isEmpty() ? "<gray>none" : String.join(", ", group.getSubcommands()))));
                String perm = group.getPermission();
                sender.sendMessage(mm(configCache.prefix + "<gold>Permission: <white>" + (perm != null ? perm : "<gray>none (default group)")));
            } else {
                sender.sendMessage(mm(configCache.prefix + "<gold>Available groups:"));
                for (String groupName : configCache.getGroupNames()) {
                    CWGroup group = configCache.getGroup(groupName);
                    String perm = group.getPermission();
                    sender.sendMessage(mm(configCache.prefix + "  <white>- <gold>" + groupName + (perm != null ? " <gray>(" + perm + ")" : " <gray>(default)")));
                }
            }
            return true;
        }
        sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().noPermission));
        return true;
    }

    private boolean handleDump(CommandSender sender) {
        if (!(sender instanceof Player) || sender.hasPermission(CWPermission.DUMP.permission())) {
            File dumpFile = new File(plugin.getDataFolder(), "commands_dump.yml");
            try {
                Files.createDirectories(dumpFile.getParentFile().toPath());
                if (!dumpFile.exists()) {
                    dumpFile.createNewFile();
                }

                List<String> allCommands = getAllServerCommands();
                Collections.sort(allCommands);

                FileConfiguration dumpConfig = new YamlConfiguration();
                dumpConfig.set("commands", allCommands);
                dumpConfig.save(dumpFile);

                sender.sendMessage(mm(plugin.getConfigCache().prefix + "<green>Dumped " + allCommands.size() + " commands to commands_dump.yml"));
            } catch (IOException e) {
                sender.sendMessage(mm(plugin.getConfigCache().prefix + "<red>Failed to dump commands: " + e.getMessage()));
            }
            return true;
        }
        sender.sendMessage(mm(plugin.getConfigCache().prefix + plugin.getConfigCache().noPermission));
        return true;
    }

    private List<String> getAllServerCommands() {
        List<String> commands = new ArrayList<>();
        try {
            CommandMap commandMap = getCommandMap();
            if (commandMap != null) {
                commands.addAll(commandMap.getKnownCommands().keySet());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not get command map: " + e.getMessage());
        }
        return commands;
    }

    private CommandMap getCommandMap() {
        try {
            Field field = plugin.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(plugin.getServer());
        } catch (Exception e) {
            return null;
        }
    }

    private void sendHelp(CommandSender sender) {
        ConfigCache configCache = plugin.getConfigCache();
        sender.sendMessage(mm(configCache.prefix + "<gold>anyNoCMDS Commands:"));
        sender.sendMessage(mm(configCache.prefix + "  <white>/anynocmds reload <gray>- Reload configuration"));
        sender.sendMessage(mm(configCache.prefix + "  <white>/anynocmds whitelist <group> <add|remove> <command> <gray>- Manage command whitelist"));
        sender.sendMessage(mm(configCache.prefix + "  <white>/anynocmds blacklist <group> <add|remove> <subcommand> <gray>- Manage subcommand blacklist"));
        sender.sendMessage(mm(configCache.prefix + "  <white>/anynocmds list [group] <gray>- List groups or group details"));
        sender.sendMessage(mm(configCache.prefix + "  <white>/anynocmds dump <gray>- Dump all server commands to file"));
        sender.sendMessage(mm(configCache.prefix + "  <white>/anynocmds help <gray>- Show this help"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "whitelist", "blacklist", "list", "dump", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("whitelist") || subCommand.equals("blacklist") || subCommand.equals("list")) {
                completions.addAll(plugin.getConfigCache().getGroupNames());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("whitelist") || subCommand.equals("blacklist")) {
                completions.addAll(Arrays.asList("add", "remove"));
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            String groupName = args[1].toLowerCase();
            CWGroup group = plugin.getConfigCache().getGroup(groupName);
            if (group != null) {
                if (subCommand.equals("whitelist")) {
                    completions.addAll(getAllServerCommands());
                } else if (subCommand.equals("blacklist")) {
                    for (String cmd : group.getCommands()) {
                        if (!cmd.equals("*")) {
                            completions.add(cmd + " ");
                        }
                    }
                }
            }
        }

        return CommandUtil.filterSuggestions(
            String.join(" ", args),
            completions,
            Collections.emptyList()
        );
    }
}