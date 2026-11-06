# Polymorphic Mekanism

**This is a compatible mod. It Enables [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism)'s Formulaic Assemblicator, QIO, and Robit to use the [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) feature.**

**You can now save and modify conflicting recipes in Formula Assemblicator, but you need to turn off automatic mode first.**

![image](https://media.forgecdn.net/attachments/description/1781223/description_8c271104-bc2e-43c6-80ca-493ffb6bea11.png)

**As is well known, the QIO dashboard can open three GUI like crafting table, which can now freely switch conflicting recipes without interfering with each other.**

![image](https://media.forgecdn.net/attachments/description/1781223/description_d10b336d-4d4b-4d60-a397-c5d19fafcdc9.png)

**The robit's crafting table is automatically compatible with Polymorph, so there is no need to make too many adjustments, but it is additionally compatible with the robit's furnace.**

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

Linux / macOS：

```sh
sh ./gradlew build
```

The build product is located in `build/libs/`. The current default file name is ` polymek-1.0.0. jar `, and the mod ID and version are determined by ` mod_id ` and ` mod_version ` in ` gradle. properties `.

## 许可证

This project adopts [MIT License](LICENSE)，and the copyright belongs to Lost Myself. Third party dependencies follow their respective licenses.