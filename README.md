# MiniHUD Bridge Listener (v1.0, MC 1.21.4)

Client-only Fabric add-on that enables MiniHUD debug overlays on newer Paper servers by subscribing to a custom debug channel provided by the MiniHUD Debug Bridge plugin.

## Credits
- MiniHUD and malilib by maruohon (original mod and rendering logic)
- Servux by maruohon (inspiration for server-assisted debug features)

This project is not affiliated with or endorsed by the original authors.

## What It Does
- Sends a lightweight subscribe packet after joining.
- Receives debug data over a custom plugin channel.
- Feeds the data into Minecraft's built-in debug renderers so MiniHUD can display overlays.

## Requirements
- Minecraft (for my example I used client version 1.21.4)
- Fabric Loader
- Fabric API
- MiniHUD 0.34.7
- malilib 0.23.5
- MiniHUD Debug Bridge plugin installed on the Paper server

## Install (Client)
1. Drop the addon jar into your `mods` folder.
2. Ensure MiniHUD and malilib are installed.
3. Join the server and enable the debug overlays in MiniHUD. (Make sure you are OP)

## Build
1. Place MiniHUD and malilib jars in `libs/` (see `libs/README.txt`).
2. Run `./gradlew build`.

## Adapting To Another Minecraft Version
1. Update `gradle.properties` with the target `minecraft_version`, `yarn_mappings`, `loader_version`, and `fabric_version`.
2. Replace the MiniHUD and malilib jars in `libs/` with matching versions.
3. Review `MinihudDebugAddon` payload parsing if Mojang changes debug renderer payload fields.
4. Rebuild with `./gradlew build`.

## Notes
- This addon does not change or replace MiniHUD. It only supplies server debug data.
- For native 1.21.11 clients, MiniHUD can receive debug data without this addon.

## License
GNU LGPL v3.0 (see `LICENSE`).
