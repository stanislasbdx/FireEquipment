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

## Resource pack

The tools use the `item_model` component (`fireequipment:hose`, `fireequipment:pump`, `fireequipment:extinguisher`), so renaming an item or changing its texture does not affect them.
The pack lives in `resourcepack/` and is built as `FireEquipment-pack-<version>.zip`. It is attached to every GitHub release together with the plugin jar, and it supports Minecraft 1.21.4 and newer (pack format 46 to 88).

Tools given before the pack existed are updated automatically when their owner joins or uses them.

To use your own models, change `Equipment.<Tool>.itemModel` in `config.yml` (empty means the vanilla look).
To have the plugin send the pack to players on join, fill the `ResourcePack` section:

```yaml
ResourcePack:
  enabled: true
  url: "https://example.com/FireEquipment-pack-2.2.0.zip"
  sha1: "<sha1 of the zip, printed by the build and written in the release notes>"
  prompt: "FireEquipment textures"
  force: false
```

## Building

```
./gradlew build -x sonar
```

The plugin jar is `build/libs/FireEquipment-<version>.jar` (bStats is shaded and relocated) and the resource pack is `build/resourcepack/FireEquipment-pack-<version>.zip`.
Pushing a tag named like the version in `build.gradle` (for example `2.2.0`) builds both and attaches them to the GitHub release.
Tests use JUnit 5 and MockBukkit.
