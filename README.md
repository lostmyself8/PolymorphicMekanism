# Polymorphic Mekanism

**This compatibility mod brings [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) recipe selection to [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism)'s Formulaic Assemblicator, QIO, and Robit.**

**You can select and save conflicting recipes in the Formulaic Assemblicator. Turn off automatic mode before changing the selected recipe.**

![image](https://media.forgecdn.net/attachments/description/1781223/description_8c271104-bc2e-43c6-80ca-493ffb6bea11.png)

**The QIO Dashboard has three crafting windows, each of which can switch between conflicting recipes independently.**

![image](https://media.forgecdn.net/attachments/description/1781223/description_d10b336d-4d4b-4d60-a397-c5d19fafcdc9.png)

**The Robit's Crafting Table is already compatible with Polymorph. This mod also adds recipe selection to the Robit's Furnace.**

![image](https://media.forgecdn.net/attachments/description/1781223/description_fe8cfce6-f447-4341-9e38-93dee3b09447.png)

## Features

| Integration | Function |
| --- | --- |
| Formulaic Assemblicator | Select between conflicting recipes. The selected recipe is saved when encoding a formula, and remains selected when that formula is used in another Formulaic Assemblicator. |
| QIO Crafting | Supports the QIO Dashboard and Portable QIO Dashboard. Each of the three crafting windows saves its selection independently, and the button moves with the native window. |
| Robit Furnace | Select between conflicting smelting recipes. Switching recipes for the same input preserves smelting progress, and the selection is saved with the Robit. |

Polymorph natively supports the Robit Crafting Table. This mod only provides additional integration for the Robit Furnace.

## Building from Source

Use JDK 21 and run the Gradle Wrapper included in the project root; a separate Gradle installation is not required. The first build requires an internet connection to download dependencies.

Windows PowerShell:

```powershell
.\gradlew.bat build
```

Linux / macOS:

```sh
sh ./gradlew build
```

The build artifact is located in `build/libs/`. The default filename is `polymek-1.0.0.jar`; the mod ID and version are set by `mod_id` and `mod_version` in `gradle.properties`.

## License

This project is licensed under the [MIT License](LICENSE). Copyright belongs to Lost Myself. Third-party dependencies are licensed under their respective terms.
