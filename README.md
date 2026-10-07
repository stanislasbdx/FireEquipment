# FireEquipment

Roleplay tools for firefighter gameplay on Spigot, Paper and Purpur (Minecraft 1.21+).

## Tools

| Tool | Item | Right-click effect | Permission |
| --- | --- | --- | --- |
| Hose | Golden hoe | Throws water that disappears after a short delay | `firequip.tools.hose` |
| Pump | Clay ball | Removes nearby water | `firequip.tools.pump` |
| Extinguisher | Iron hoe | Puts out fire | `firequip.tools.extinguisher` |

Tools are recognised by a persistent data tag, so renaming an item or changing its texture does not affect them.
Block changes go through `BlockPlaceEvent` / `BlockBreakEvent`, so region protection plugins are respected.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/firequip help` | Help page | `firequip.info` |
| `/firequip version` | Plugin version | `firequip.info` |
| `/firequip reload` | Reload `config.yml` | `firequip.admin.reload` |
| `/fequip <hose\|pump\|extinguisher>` | Give yourself a tool | `firequip.tools.give` |

## Configuration

`config.yml` is created on first start and upgraded automatically (missing keys are added) when the plugin version changes.

```yaml
Equipment:
  Hose:
    displayName: "Fire Hose"
    usage: "Right-click to throw water"
    range: 5
    cooldown: 10
    waterLifetime: 60
```

`range` is in blocks, `cooldown` and `waterLifetime` are in ticks. Messages accept legacy `&` colour codes and, on Paper, MiniMessage tags.

## Building

```
./gradlew build -x sonar
```

The plugin jar is `build/libs/FireEquipment-<version>.jar` (bStats is shaded and relocated).
Tests use JUnit 5 and MockBukkit.
