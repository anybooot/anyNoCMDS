# anyNoCMDS

**A lightweight command management plugin for Minecraft Paper servers.**

anyNoCMDS is a command filtering plugin that gives server administrators control over which commands players can execute, based on their LuckPerms permission groups.

It provides whitelist and blacklist functionality, allowing you to manage command access and keep your server organized and secure.

## Features

- **Group-Based Filtering** – Configure command restrictions based on LuckPerms permission groups.
- **Whitelist System** – Allow only explicitly permitted commands.
- **Blacklist System** – Block specific commands while allowing everything else.
- **LuckPerms Integration** – Uses LuckPerms to identify player permission groups.
- **Runtime Management** – Manage whitelist and blacklist settings through in-game commands.
- **Configuration Reloading** – Reload plugin settings without restarting the server.
- **Administrative Tools** – Includes commands for listing and dumping plugin data.

## Requirements

- Minecraft 1.20+
- Paper (1.20.4 API)
- Java 17+
- [LuckPerms](https://luckperms.net/) 5.5+

## Installation

1. Download the latest `anyNoCMDS.jar` from the [Releases](https://github.com/anybooot/anyNoCMDS/releases) page.
2. Stop your Minecraft server.
3. Place the JAR file inside your server's `plugins` folder.
4. Make sure LuckPerms is installed.
5. Start your server.

The plugin will load automatically.

## Commands

Main command:

```text
/anynocmds
```

Aliases:

```text
/anc
/cmdwhitelist
/cw
```

Available subcommands:

| Command | Description |
|---|---|
| `/anynocmds help` | Displays available commands |
| `/anynocmds reload` | Reloads plugin configuration |
| `/anynocmds whitelist` | Manages the whitelist |
| `/anynocmds blacklist` | Manages the blacklist |
| `/anynocmds list` | Lists configured entries |
| `/anynocmds dump` | Dumps plugin data |

## Permissions

| Permission | Description |
|---|---|
| `anynocmds.admin` | Allows access to the main plugin command |

## Dependencies

**Required:**
- [LuckPerms](https://luckperms.net/)

**Optional:**
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)

## Building from Source

This project uses Maven.

Clone the repository:

```bash
git clone https://github.com/anybooot/anyNoCMDS.git
```

Navigate to the project directory:

```bash
cd anyNoCMDS
```

Build the plugin:

```bash
mvn clean package
```

The compiled JAR will be available inside:

```text
target/
```

## Compatibility

| Component | Version |
|---|---|
| Java | 17+ |
| Minecraft API | Paper 1.21+ |
| LuckPerms API | 5.5 |

## Support & Issues

Found a bug or have a suggestion?

- [Report a Bug](https://github.com/anybooot/anyNoCMDS/issues)
- [Request a Feature](https://github.com/anybooot/anyNoCMDS/issues)
- [View Source Code](https://github.com/anybooot/anyNoCMDS)

## License

See the repository for license information.

---

**Developed by [Anyboot](https://github.com/anybooot).**
