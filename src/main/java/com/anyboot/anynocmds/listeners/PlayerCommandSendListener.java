package com.anyboot.anynocmds.listeners;

import com.anyboot.anynocmds.AnyNoCMDS;
import com.anyboot.anynocmds.config.ConfigCache;
import com.anyboot.anynocmds.config.CWPermission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerCommandSendListener implements Listener {

    private final AnyNoCMDS plugin;

    public PlayerCommandSendListener(AnyNoCMDS plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(CWPermission.BYPASS.permission())) {
            return;
        }

        ConfigCache configCache = plugin.getConfigCache();
        Set<String> allowedCommands = getAllowedCommands(player);

        if (configCache.hideUnknownCommands) {
            // Remove commands that the player doesn't have access to
            event.getCommands().removeIf(cmd -> !allowedCommands.contains(cmd.toLowerCase()));
        }
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
}