# Equipment item textures

FireEquipment does **not** ship a resource pack. The hose, the pump, and the extinguisher are vanilla items with `CustomModelData`. The PNGs in this repo are ready to drop into a normal pack or into [Nexo](https://docs.nexomc.com).

The plugin already writes the matching model data (legacy integer **and** the 1.21.4+ `custom_model_data` floats component). Change `config.yml` only if you change the IDs in the pack.

## Files

| Path | Use |
| :--- | :--- |
| `assets/textures/item/*.png` | 512×512 RGBA files the pack must use |
| `assets/textures/item/hi/*.png` | Larger sources. Leave them out of the pack |

Item textures are square PNGs, transparent background, `minecraft:item/generated`. They are isometric pixel-art sprites. Plates on the sprites are blank white strokes, not readable labels.

## What the plugin puts on items

| Item | Material | `CustomModelData` | Texture |
| :--- | :--- | :--- | :--- |
| Extinguisher | `IRON_HOE` | `1` | `extinguisher.png` |
| Hose | `GOLDEN_HOE` | `1` | `hose.png` |
| Pump | `CLAY_BALL` | `1` | `pump.png` |

The three materials differ, so model data `1` does not collide between them. A pack that already uses `1` on an iron hoe, a golden hoe, or a clay ball must change the id in both `config.yml` and the pack.

```yaml
Equipment:
  Extinguisher:
    material: IRON_HOE
    custom-model-data: 1    # CustomModelData and custom_model_data also work
  Hose:
    material: GOLDEN_HOE
    custom-model-data: 1
  Pump:
    material: CLAY_BALL
    custom-model-data: 1
```

A missing key defaults to `1`. `0` or a negative value disables model data for that item. The tool stays the vanilla material.

Tools do not glow. There is no enchantment glint. They are unbreakable so the hoes do not wear down.

Do **not** `/nexo give` these items as the tools players use. FireEquipment items carry PDC (`firequip` = `item-type-Hose`, `item-type-Pump`, or `item-type-Extinguisher`). The pack only changes how the vanilla material + model data **look**. Give them with `/fequip`.

---

## 1. Normal resource pack (1.21.4+)

Namespace used below: `fireequipment`. Folder layout:

```
fireequipment/
├── pack.mcmeta
└── assets/
    ├── minecraft/items/
    │   ├── iron_hoe.json
    │   ├── golden_hoe.json
    │   └── clay_ball.json
    └── fireequipment/
        ├── models/item/
        │   ├── extinguisher.json
        │   ├── hose.json
        │   └── pump.json
        └── textures/item/
            ├── extinguisher.png
            ├── hose.png
            └── pump.png
```

Copy the 512×512 files from `assets/textures/item/` into `assets/fireequipment/textures/item/`. Leave `hi/` out of the pack.

`pack.mcmeta` — set `pack_format` for your Minecraft version ([wiki](https://minecraft.wiki/w/Pack_format)). `46` is 1.21.4:

```json
{
  "pack": {
    "pack_format": 46,
    "description": "FireEquipment"
  }
}
```

Each custom model (`assets/fireequipment/models/item/extinguisher.json`, same idea for the hose and the pump):

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "fireequipment:item/extinguisher"
  }
}
```

`assets/minecraft/items/iron_hoe.json`:

```json
{
  "model": {
    "type": "minecraft:range_dispatch",
    "property": "minecraft:custom_model_data",
    "entries": [
      { "threshold": 1, "model": { "type": "minecraft:model", "model": "fireequipment:item/extinguisher" } }
    ],
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/iron_hoe"
    }
  }
}
```

Same pattern for `golden_hoe.json` (`threshold` 1 → `fireequipment:item/hose`) and `clay_ball.json` (`threshold` 1 → `fireequipment:item/pump`). Always keep the vanilla fallback so a normal hoe or clay ball stays vanilla.

Zip the folder (zip **the contents**, so `pack.mcmeta` is at the root of the zip), then either:

- put it in `server.properties` `resource-pack=`, or
- serve it however you already send a pack to players.

On 1.21.3 and older, item definitions live under `assets/minecraft/models/item/<item>.json` with `overrides` / `custom_model_data` predicates instead of `assets/minecraft/items/`. Prefer the `items/` format above on current Paper.

---

## 2. Nexo

Nexo builds and sends the pack. Two ways; pick one.

### A. Native Nexo items (pack generation)

1. Copy the 512×512 PNGs to:

   `plugins/Nexo/pack/assets/fireequipment/textures/item/`

   Example: `plugins/Nexo/pack/assets/fireequipment/textures/item/extinguisher.png`  
   In YAML that path is `fireequipment:item/extinguisher` (no `textures/`, no `.png`). See [Nexo FAQ](https://docs.nexomc.com/general-usage/faq).

2. Add `plugins/Nexo/items/fireequipment.yml` (Nexo merges every file in `items/`). IDs are only so Nexo generates the pack; hide them from the Nexo catalog.

```yaml
fe_extinguisher:
  material: IRON_HOE
  excludeFromInventory: true
  Pack:
    texture: fireequipment:item/extinguisher
    custom_model_data: 1

fe_hose:
  material: GOLDEN_HOE
  excludeFromInventory: true
  Pack:
    texture: fireequipment:item/hose
    custom_model_data: 1

fe_pump:
  material: CLAY_BALL
  excludeFromInventory: true
  Pack:
    texture: fireequipment:item/pump
    custom_model_data: 1
```

`Pack.custom_model_data` must equal `Equipment.*.custom-model-data` in FireEquipment. Nexo then injects a `custom_model_data` range into the vanilla item model, so an iron hoe with model data `1` shows `extinguisher.png`.

3. Reload the pack (`/nexo pack reload` or a restart) and reconnect so the client downloads it.

Docs: [Items](https://docs.nexomc.com/configuration/items), [Resource pack](https://docs.nexomc.com/configuration/resourcepack).

### B. Drop the vanilla pack into Nexo

Build the zip from section 1, then put the zip **or** the unpacked folder in:

`plugins/Nexo/pack/external_packs/`

Nexo merges it into the pack it sends. Skip the YAML in A if you do this, so you do not define the same model data twice.

---

## Checklist

- PNG is 512×512, RGBA, transparent around the sprite.
- Texture path / Nexo `Pack.texture` uses namespace `fireequipment` and the file name without `.png`.
- Material + `CustomModelData` in the pack match `config.yml`.
- Vanilla fallback is kept for each overridden item.
- Players still receive tools from `/fequip`, not from Nexo give.
- After a pack change, reconnect (or force the pack to redistribute) and check the extinguisher, the hose, and the pump.
