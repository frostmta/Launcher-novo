<div align="center">

<img src="docs/zadrinth-icon.png" alt="Zadrinth Launcher icon" width="128"/>

# Zadrinth Launcher

**A Minecraft: Java Edition launcher for Android, inspired by the Modrinth App.**

![License](https://img.shields.io/badge/license-GPL--3.0-9D7BFF)
![Android](https://img.shields.io/badge/Android-8.0%2B-6E9BFF)
![Status](https://img.shields.io/badge/status-in%20development-E07BFF)

[Português](README.md) | English

</div>

> [!IMPORTANT]
> Zadrinth Launcher is an **independent, unofficial** project. It is **not affiliated** with Modrinth (Rinth, Inc.), the official Zalith Launcher, Mojang or Microsoft. "Modrinth" and "Minecraft" are trademarks of their respective owners.
>
> This app is an **unofficial modified version** of [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2). Please report issues **in this repository**, not to Zalith.

## ✨ About

Zadrinth Launcher brings the look and flow of the **Modrinth App** (desktop) to Android phones, while keeping the game engine from Zalith Launcher 2 / PojavLauncher (LWJGL, renderers, on-screen controls and official Microsoft login).

Open the launcher, pick an instance and play, with Modrinth mods, modpacks, resource packs and shaders installed right from the app.

## 🎨 Look

- Dark theme with a **blue → violet → pink** gradient and purple-tinted backgrounds
- Floating side bar: your account, **Home**, **Library**, **Explore**, **Wardrobe**, **Multiplayer**, recent instances and **Settings**
- **Wardrobe** with a 3D character preview, official Minecraft capes, original Zadrinth capes and cape/skin import for offline accounts
- **Explore** with top tabs and Modrinth-style result cards; settings with a side menu
- Rounded cards with thin borders and a gradient **Play** button

## 🗺️ Roadmap

| Stage | What it adds | Status |
|---|---|---|
| 1 | Design system, side bar and **Library** screen | ✅ Done ([PR #1](https://github.com/frostmta/Launcher-novo/pull/1)) |
| 2 | **Isolated instances** (`mods/`, `config/`, `saves/`... + `instance.json`) and an instance page with Content, Worlds, Logs and Settings tabs | 🔜 |
| 3 | **Explore** screen using the Modrinth API (Labrinth v2): search and filters by version, loader and category | 🔜 |
| 4 | Install into any instance, with **required dependencies** downloaded automatically | 🔜 |
| 5 | Full **`.mrpack`** support (SHA-1/SHA-512 hashes and `overrides/`) | 🔜 |

## 📥 Download

There is no stable release yet. Test APKs are built automatically in the [Actions](https://github.com/frostmta/Launcher-novo/actions) tab (open the latest run and download the `debug apk` artifact; **arm64** fits most phones).

Stable versions will be published under [Releases](https://github.com/frostmta/Launcher-novo/releases).

## 🛠️ Building

The easiest way is GitHub Actions: every push builds the debug APKs.

To build locally you need:

- JDK 21
- Android SDK (API 37) and NDK 27
- Rust with the Android targets and [`cargo-ndk`](https://github.com/bbqsrc/cargo-ndk)

```bash
git clone https://github.com/frostmta/Launcher-novo.git
cd Launcher-novo
./gradlew ZalithLauncher:assembleDebug -Darch=arm64
```

Optional keys (environment variables or GitHub secrets): `OAUTH_CLIENT_ID` (Microsoft login) and `CURSEFORGE_API_KEY` (CurseForge downloads).

## 📜 License

This project is released under the **[GPL-3.0](LICENSE)**, the same license as Zalith Launcher 2.

It follows Zalith Launcher 2's additional terms (GPLv3, section 7):

- the program name was changed to tell it apart from the original;
- the app shows it is an **unofficial modified version** on its main screen;
- the copyright notices shown by the program were kept.

## 🙏 Credits

- **[Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)** by MovTery and contributors: the base of this whole project
- **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)**: the game engine used by Zalith
- **[Modrinth](https://modrinth.com)**: inspiration for the app's look and flow, and the public API used to browse content

### Open-source libraries

This software uses the following open-source libraries (list inherited from Zalith Launcher 2):

| Library                               | Copyright                                                                                                     | License              | Official Link                                                                      |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|------------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [Link↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [Link↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Aoache 2.0           | [Link↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [Link↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [Link↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [Link↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [Link↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [Link↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [Link↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Mesa                                  | Copyright © The Mesa Authors                                                                                  | MIT License          | [Link↗](https://mesa3d.org/)                                                       |
| MobileGlues                           | Copyright © 2025-2026 MobileGL-Dev                                                                           | LGPL-2.1 License     | [Link↗](https://github.com/MobileGL-Dev/MobileGlues)                             |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [Link↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| NG-GL4ES                              | Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright (c) 2025-2026 BZLZHH | MIT License          | [Link↗](https://github.com/BZLZHH/NG-GL4ES)                                        |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://square.github.io/okio/)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [Link↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [Link↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [Link↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [Link↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [Link↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [Link↗](https://github.com/libsdl-org/SDL)                                         |
| SimpleFPEWrapper                      | Copyright © 2026 MobileGL-Dev                                                                                | LGPL-3.0 License     | [Link↗](https://github.com/MobileGL-Dev/SimpleFPEWrapper)                        |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [Link↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [Link↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [Link↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [Link↗](https://tukaani.org/xz/java.html)                                          |
