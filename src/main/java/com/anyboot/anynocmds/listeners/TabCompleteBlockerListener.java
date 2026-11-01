package com.anyboot.anynocmds.listeners;

import com.anyboot.anynocmds.AnyNoCMDS;
import com.anyboot.anynocmds.config.CommandUtil;
import com.anyboot.anynocmds.config.ConfigCache;
import com.anyboot.anynocmds.config.CWPermission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.HashSet;
import java.util.Set;

public class TabCompleteBlockerListener implements Listener {

    private final AnyNoCMDS plugin;

    public TabCompleteBlockerListener(AnyNoCMDS plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) {
            return;
        }

        if (player.hasPermission(CWPermission.BYPASS.permission())) {
            return;
        }

        String buffer = event.getBuffer();
        if (buffer == null || buffer.isEmpty() || buffer.charAt(0) != '/') {
            return;
        }

        int commandEnd = buffer.indexOf(' ');
        String commandLabel;
        if (commandEnd < 0) {
            commandLabel = buffer.substring(1).toLowerCase();
        } else {
            commandLabel = buffer.substring(1, commandEnd).toLowerCase();
        }

        ConfigCache configCache = plugin.getConfigCache();
        Set<String> allowedCommands = getAllowedCommands(player);

        if (!allowedCommands.contains(commandLabel)) {
            configCache.debug("Blocked tab completion for " + player.getName() + " on command: " + commandLabel);
            event.setCancelled(true);
            return;
        }

        if (event.getCompletions().isEmpty()) {
            return;
        }

        Set<String> blockedSubcommands = getBlockedSubcommands(player);
        event.setCompletions(CommandUtil.filterSuggestions(buffer, event.getCompletions(), blockedSubcommands));
    }

    private Set<String> getAllowedCommands(Player player) {
        Set<String> commands = new HashSet<>();
        ConfigCache configCache = plugin.getConfigCache();

        for (String groupName : configCache.getGroupNames()) {
            var group = configCache.getGroup(groupName);
            if (group == null) continue;

            String permission = group.getPermission();
            if (permission == null || player.hasPermission(permission)) {
                commands.addAll(group.getCommands());
            }
        }

        return commands;
    }

    private Set<String> getBlockedSubcommands(Player player) {
        Set<String> subcommands = new HashSet<>();
        ConfigCache configCache = plugin.getConfigCache();

        for (String groupName : configCache.getGroupNames()) {
            var group = configCache.getGroup(groupName);
            if (group == null) continue;

            String permission = group.getPermission();
            if (permission == null || player.hasPermission(permission)) {
                subcommands.addAll(group.getSubcommands());
            }
        }

        return subcommands;
    }
}