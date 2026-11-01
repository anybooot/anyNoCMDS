package com.anyboot.anynocmds.config;

public enum CWPermission {
    BYPASS("anynocmds.bypass"),
    ADMIN("anynocmds.admin"),
    RELOAD("anynocmds.reload"),
    WHITELIST("anynocmds.whitelist"),
    BLACKLIST("anynocmds.blacklist"),
    LIST("anynocmds.list"),
    DUMP("anynocmds.dump");

    private final String permission;

    CWPermission(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return permission;
    }
}