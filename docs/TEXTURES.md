# Item textures

FireEquipment ships a ready-to-use resource pack. The hose, pump and extinguisher are vanilla items (golden hoe, clay ball, iron hoe) with the `item_model` component, so renaming an item or changing its texture never affects them. The plugin recognises them by their persistent data tag, not by their look.

The pack is in `resourcepack/` and is built as `FireEquipment-pack-<version>.zip`. Every GitHub release has the plugin jar and this zip attached. The SHA-1 of the zip is in the release notes.

## Files

| Path | Size | Use |
| :--- | :--- | :--- |
| `resourcepack/assets/fireequipment/textures/item/*.png` | 64×64 RGBA | Item textures |
| `resourcepack/assets/fireequipment/models/item/*.json` | | `minecraft:item/generated` models |
| `resourcepack/assets/fireequipment/items/*.json` | | Item definitions used by `item_model` |
| `resourcepack/pack.mcmeta` | | Pack metadata (formats 46 to 88) |

Textures are square PNGs with a transparent background, flat sprites on `minecraft:item/generated`. If you edit them, downscale with nearest-neighbour, never bilinear.

## What the plugin puts on items

| Tool | Material | `item_model` | Texture |
| :--- | :--- | :--- | :--- |
| Hose | `GOLDEN_HOE` | `fireequipment:hose` | `hose.png` |
| Pump | `CLAY_BALL` | `fireequipment:pump` | `pump.png` |
| Extinguisher | `IRON_HOE` | `fireequipment:extinguisher` | `extinguisher.png` |

Config keys. Change them only if you change the model in the pack:

```yaml
Equipment:
  Hose:
    itemModel: "fireequipment:hose"          # empty = vanilla look
  Pump:
    itemModel: "fireequipment:pump"
  Extinguisher:
    itemModel: "fireequipment:extinguisher"
```

Invalid keys are ignored, and the item keeps the vanilla look. Tools given before the pack existed are updated when their owner joins or uses them, so there is nothing to give again.

Do **not** `/nexo give` (or any other plugin's give command) these items as the tools players use. FireEquipment items carry PDC (`firequip` = `item-type-Hose`, `item-type-Pump`, `item-type-Extinguisher`). Give them with `/fequip <hose|pump|extinguisher>`. The pack only changes how they **look**.

---

## 1. Normal resource pack (1.21.4+)

Pack formats, from the [wiki](https://minecraft.wiki/w/Pack_format):

| Minecraft | Resource pack format |
| :--- | :--- |
| 1.21.4 | 46 |
| 1.21.5 | 55 |
| 1.21.6 | 63 |
| 1.21.7 – 1.21.8 | 64 |
| 1.21.9 – 1.21.10 | 69.0 |
| 1.21.11 | 75.0 |
| 26.1 – 26.1.2 | 84.0 |
| 26.2 | 88.0 |

The shipped `pack.mcmeta` covers 46 to 88. Because it supports formats below 65, it needs both `pack_format` / `supported_formats` (older clients) and `min_format` / `max_format` (1.21.9+):

```json
{
  "pack": {
    "description": "FireEquipment - textures des outils pompier",
    "pack_format": 46,
    "supported_formats": {
      "min_inclusive": 46,
      "max_inclusive": 88
    },
    "min_format": 46,
    "max_format": 88
  }
}
```

On a newer Minecraft the game shows the pack as incompatible but you can still enable it. Raise `max_format` when a new version is released and the pack still works.

Folder layout:

```
FireEquipment-pack/
├── pack.mcmeta
├── pack.png
└── assets/fireequipment/
    ├── items/
    │   ├── hose.json
    │   ├── pump.json
    │   └── extinguisher.json
    ├── models/item/
    │   ├── hose.json
    │   ├── pump.json
    │   └── extinguisher.json
    └── textures/item/
        ├── hose.png
        ├── pump.png
        └── extinguisher.png
```

Item definition, `assets/fireequipment/items/hose.json` (same idea for the other two):

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "fireequipment:item/hose"
  }
}
```

Model, `assets/fireequipment/models/item/hose.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "fireequipment:item/hose"
  }
}
```

Unlike a `CustomModelData` setup, nothing under `assets/minecraft/items/` is overridden. `item_model` points straight at the `fireequipment` namespace, so vanilla golden hoes, clay balls and iron hoes are never touched and there is no vanilla fallback to maintain.

Then pick one:

- Put the zip on your own host and set `resource-pack=` and `resource-pack-sha1=` in `server.properties`.
- Let FireEquipment send it on join (off by default):

  ```yaml
  ResourcePack:
    enabled: true
    url: "https://example.com/FireEquipment-pack-<version>.zip"
    sha1: "<sha1 from the release notes>"
    prompt: "FireEquipment textures"
    force: false
  ```

- Merge `assets/fireequipment/` into a pack you already send. Zip **the contents**, so `pack.mcmeta` is at the root of the zip.

Use `./gradlew resourcePackZip` to build the zip yourself. It prints the SHA-1.

---

## 2. Nexo

Nexo builds and sends the pack. The plugin already puts `item_model` on the tools, and the pack already defines `fireequipment:hose`, `fireequipment:pump` and `fireequipment:extinguisher`, so you do **not** need to create Nexo items. Import the zip.

### A. Drop the pack into Nexo (recommended)

1. Download `FireEquipment-pack-<version>.zip` from the release.

2. Put the zip **or** the unpacked folder in:

   `plugins/Nexo/pack/external_packs/`

   Nexo merges it into the pack it sends. See [Resource pack](https://docs.nexomc.com/configuration/resourcepack).

3. Rebuild and resend the pack:

   ```
   /nexo reload pack
   ```

   Then reconnect so the client downloads it.

4. Keep `ResourcePack.enabled: false` in FireEquipment's `config.yml`. Nexo is the one sending the pack, and you do not want two plugins sending a pack to the same player. How the pack reaches players (`NONE`, `POLYMATH`, `SELFHOST`, `LOBFILE`, `S3`) is set in Nexo with `Pack.server.type`.

Nexo can also import from a place other than the folder, with `Plugin.import.from_location` (a directory or zip relative to `plugins/`) or `Plugin.import.from_url` (any URL Nexo can download a directory or zip from) in its `settings.yml`.

### B. Use your own Nexo model instead (advanced)

If you would rather draw the tools with Nexo, create a Nexo item with the texture you want, then point FireEquipment at the model Nexo generates:

```yaml
Equipment:
  Hose:
    itemModel: "nexo:<model key from your Nexo pack>"
```

Nexo's own template uses `Components: item_model: nexo:<item_id>` ([Items](https://docs.nexomc.com/configuration/items)), so the key is probably `nexo:<item_id>`. Check the generated pack to be sure. Do not also keep the FireEquipment pack in `external_packs`, so you do not define the same model twice. The Nexo item is only there to make Nexo generate the model, hide it from the catalogue and never give it to players.

---

## Checklist

- PNG is 64×64, RGBA, transparent around the sprite.
- `item_model` in `config.yml` matches the model in the pack, namespace `fireequipment`, no `.png`.
- With Nexo, FireEquipment's own `ResourcePack.enabled` is `false`.
- If you enable Nexo's obfuscation, test the three tools. If a texture is missing, switch it off to check.
- Players still receive the tools from `/fequip`, not from Nexo give.
- After a pack change, run `/nexo reload pack` (or force the pack to redistribute), reconnect, and check one hose, one pump and one extinguisher.
