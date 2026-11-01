package com.anyboot.anynocmds.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CWGroup {

    private final String name;
    private final Set<String> commands;
    private final Set<String> subcommands;
    private final String customCommandDeniedMessage;
    private final boolean hasWildcard;

    public CWGroup(String name, List<String> commands, List<String> subcommands, String customCommandDeniedMessage) {
        this.name = name.toLowerCase();
        this.commands = commands.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.subcommands = subcommands.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.customCommandDeniedMessage = customCommandDeniedMessage;
        this.hasWildcard = this.commands.contains("*");
    }

    public String getName() {
        return name;
    }

    public Set<String> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    public Set<String> getSubcommands() {
        return Collections.unmodifiableSet(subcommands);
    }

    public String getCustomCommandDeniedMessage() {
        return customCommandDeniedMessage;
    }

    public boolean hasWildcard() {
        return hasWildcard;
    }

    public boolean hasCommand(String command) {
        if (hasWildcard) return true;
        return commands.contains(command.toLowerCase());
    }

    public boolean hasSubcommand(String subcommand) {
        return subcommands.contains(subcommand.toLowerCase());
    }

    public String getPermission() {
        if (name.equals("default")) {
            return null;
        }
        return "anynocmds.group." + name;
    }
}