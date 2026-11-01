package com.anyboot.anynocmds.listeners;

import com.anyboot.anynocmds.AnyNoCMDS;
import com.anyboot.anynocmds.config.CommandUtil;
import com.anyboot.anynocmds.config.ConfigCache;
import com.anyboot.anynocmds.config.CWPermission;
import com.anyboot.anynocmds.config.MessageType;
import com.anyboot.anynocmds.config.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerCommandPreProcessListener implements Listener {

    private final AnyNoCMDS plugin;

    public PlayerCommandPreProcessListener(AnyNoCMDS plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(CWPermission.BYPASS.permission())) {
            return;
        }

        String message = event.getMessage();
        String messageWithoutSlash = message.startsWith("/") ? message.substring(1) : message;
        String commandLabel = CommandUtil.getCommandLabel(message);

        ConfigCache configCache = plugin.getConfigCache();
        Set<String> allowedCommands = getAllowedCommands(player);

        if (!allowedCommands.contains(commandLabel)) {
            event.setCancelled(true);
            sendDeniedMessage(player, configCache, commandLabel);
            return;
        }

        // Check subcommands
        Set<String> blockedSubcommands = getBlockedSubcommands(player);
        for (String blockedSubcommand : blockedSubcommands) {
            if (messageWithoutSlash.toLowerCase().startsWith(blockedSubcommand.toLowerCase())) {
                event.setCancelled(true);
                sendSubcommandDeniedMessage(player, configCache);
                return;
            }
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

    private void sendDeniedMessage(Player player, ConfigCache configCache, String commandLabel) {
        String customMessage = getCustomDeniedMessage(player);
        String finalMessage = customMessage.isEmpty() ? configCache.commandDenied : customMessage;

        Component component = MessageUtil.parse(configCache.prefix + finalMessage);
        sendMessage(player, configCache, component);
        playDeniedSound(player, configCache);
    }

    private void sendSubcommandDeniedMessage(Player player, ConfigCache configCache) {
        Component component = MessageUtil.parse(configCache.prefix + configCache.subcommandDenied);
        sendMessage(player, configCache, component);
        playDeniedSound(player, configCache);
    }

    private String getCustomDeniedMessage(Player player) {
        ConfigCache configCache = plugin.getConfigCache();
        for (String groupName : configCache.getGroupNames()) {
            var group = configCache.getGroup(groupName);
            if (group == null) continue;

            String permission = group.getPermission();
            if (permission != null && player.hasPermission(permission)) {
                String custom = group.getCustomCommandDeniedMessage();
                if (custom != null && !custom.isEmpty()) {
                    return custom;
                }
            }
        }
        return "";
    }

    private void sendMessage(Player player, ConfigCache configCache, Component component) {
        switch (configCache.messageType) {
            case CHAT:
                player.sendMessage(component);
                break;
            case ACTIONBAR:
                player.sendActionBar(component);
                break;
            case CHAT_AND_ACTIONBAR:
                player.sendMessage(component);
                player.sendActionBar(component);
                break;
        }
    }

    private void playDeniedSound(Player player, ConfigCache configCache) {
        // Sound playing requires Adventure API, skip for now
    }
}