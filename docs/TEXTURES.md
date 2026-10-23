# Item textures

FireEquipment ships a ready-to-use resource pack. The hose, pump and extinguisher are vanilla items (golden hoe, clay ball, iron hoe) with the `item_model` component. The plugin recognises them by their persistent data tag, not by their look, so renaming an item or changing its texture never affects them.

The pack is in `resourcepack/` and is built as `FireEquipment-pack-<version>.zip`. Every GitHub release has the plugin jar and this zip attached, and the SHA-1 of the zip is in the release notes.

## Files

| Path | Size | Use |
| :--- | :--- | :--- |
| `resourcepack/assets/fireequipment/textures/item/*.png` | 64×64 RGBA | Item textures |
| `resourcepack/assets/fireequipment/models/item/*.json` | | `minecraft:item/generated` models |
| `resourcepack/assets/fireequipment/items/*.json` | | Item definitions used by `item_model` |

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

Tools given before the pack existed are updated when their owner joins or uses them.

Do **not** `/nexo give` these items as the tools players use. FireEquipment items carry PDC (`firequip` = `item-type-Hose`, `item-type-Pump`, `item-type-Extinguisher`). Give them with `/fequip <hose|pump|extinguisher>`. The pack only changes how they **look**.

---

## A. Custom resource pack

Copy the `assets/fireequipment/` folder from the zip (or from `resourcepack/`) into your pack:

```
your-pack/
├── pack.mcmeta               # see the wiki for the pack format of your version
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

`pack.mcmeta` and the pack format of your Minecraft version: [Pack format](https://minecraft.wiki/w/Pack_format).

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

Nothing under `assets/minecraft/` is touched. `item_model` points straight at the `fireequipment` namespace.

If FireEquipment should send the pack itself, fill the `ResourcePack` section of its `config.yml` (off by default):

```yaml
ResourcePack:
  enabled: true
  url: "https://example.com/FireEquipment-pack-<version>.zip"
  sha1: "<sha1 from the release notes>"
  prompt: "FireEquipment textures"
  force: false
```

---

## B. Nexo

The plugin already puts `item_model` on the tools and the pack already defines `fireequipment:hose`, `fireequipment:pump` and `fireequipment:extinguisher`, so you do **not** need to create Nexo items. Import the zip.

1. Download `FireEquipment-pack-<version>.zip` from the release.

2. Put the zip **or** the unpacked folder in:

   `plugins/Nexo/pack/external_packs/`

   Nexo merges it into the pack it sends. See [Resource pack](https://docs.nexomc.com/configuration/resourcepack).

3. Rebuild and resend the pack with `/nexo reload pack`, then reconnect.

Keep `ResourcePack.enabled: false` in FireEquipment's `config.yml`, so only Nexo sends the pack.
