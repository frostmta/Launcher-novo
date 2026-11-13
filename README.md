<div align="center">

<img src="docs/zadrinth-icon.png" alt="Ícone do Zadrinth Client" width="128"/>

# Zadrinth Client

**Launcher de Minecraft: Java Edition para Android, inspirado no Modrinth App.**

![Licença](https://img.shields.io/badge/licen%C3%A7a-GPL--3.0-9D7BFF)
![Android](https://img.shields.io/badge/Android-8.0%2B-6E9BFF)
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-E07BFF)

Português | [English](README_EN_US.md)

</div>

> [!IMPORTANT]
> O Zadrinth Client é um projeto **independente e não oficial**. Ele **não tem ligação** com a Modrinth (Rinth, Inc.), com o Zalith Launcher oficial, nem com a Mojang ou a Microsoft. "Modrinth" e "Minecraft" são marcas de seus respectivos donos.
>
> Este app é uma **versão modificada não oficial** do [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2). Problemas encontrados aqui devem ser relatados **neste repositório**, não no do Zalith.

## ✨ Sobre o projeto

O Zadrinth Client é um client de Minecraft Java para celular focado em **PvP competitivo, BedWars e SkyWars**, com visual escuro de vidro, entrada rápida em servidores e (em breve) mods de PvP dentro do jogo, mantendo o motor de execução do jogo do Zalith Launcher 2 / PojavLauncher (LWJGL, renderizadores, controles na tela e login oficial da Microsoft).

A ideia é simples: abrir o launcher, escolher uma instância e jogar, com mods, modpacks, resource packs e shaders do Modrinth instalados direto pelo app.

## 🎨 Visual

- Tema escuro com degradê **azul → violeta → rosa** e fundos em tons de roxo
- Barra lateral flutuante: sua conta, **Início**, **Biblioteca**, **Servidores**, **Explorar**, **Armário**, **Multijogador**, instâncias recentes e **Configurações**
- **Início** com céu animado, botão **JOGAR** grande e faixa de servidores: um toque abre o Minecraft já entrando no servidor
- **Servidores** em destaque (Mush, Hylex, GladMC, Hypixel) com ícone, jogadores online e ping ao vivo
- **Armário** com prévia 3D do personagem, capas oficiais do Minecraft, capas originais do Zadrinth e importação de capa e skin para contas offline
- **Explorar** com abas no topo e cartões no estilo do Modrinth; configurações com menu lateral
- Cards com cantos arredondados, bordas finas e botão **Jogar** em degradê

## 🗺️ Andamento

| Etapa | O que entra | Status |
|---|---|---|
| 1 | Design System, barra lateral e tela **Biblioteca** | ✅ Pronta ([PR #1](https://github.com/frostmta/Launcher-novo/pull/1)) |
| 2 | **Instâncias isoladas** (`mods/`, `config/`, `saves/`... + `instance.json`) e tela de detalhes com abas: Conteúdo, Mundos, Logs e Configurações | 🔜 |
| 3 | Tela **Explorar** com a API do Modrinth (Labrinth v2): busca e filtros por versão, loader e categoria | 🔜 |
| 4 | Instalação em qualquer instância, com **dependências obrigatórias** baixadas automaticamente | 🔜 |
| 5 | Suporte completo a **`.mrpack`** (hashes SHA-1/SHA-512 e `overrides/`) | 🔜 |

## 📥 Download

Ainda não há uma versão estável. Enquanto isso, os APKs de teste são gerados automaticamente na aba [Actions](https://github.com/frostmta/Launcher-novo/actions) (abra a execução mais recente e baixe o artefato `debug apk`; para a maioria dos celulares, escolha **arm64**).

Quando houver versões estáveis, elas vão aparecer em [Releases](https://github.com/frostmta/Launcher-novo/releases).

## 🛠️ Como compilar

O jeito mais fácil é pelo GitHub Actions: todo push gera os APKs de debug automaticamente.

Para compilar no computador:

- JDK 21
- Android SDK (API 37) e NDK 27
- Rust com os alvos Android e o [`cargo-ndk`](https://github.com/bbqsrc/cargo-ndk)

```bash
git clone https://github.com/frostmta/Launcher-novo.git
cd Launcher-novo
./gradlew ZalithLauncher:assembleDebug -Darch=arm64
```

Chaves opcionais (como variáveis de ambiente ou *secrets* do GitHub): `OAUTH_CLIENT_ID` (login da Microsoft) e `CURSEFORGE_API_KEY` (downloads do CurseForge).

## 📜 Licença

Este projeto é distribuído sob a **[GPL-3.0](LICENSE)**, a mesma licença do Zalith Launcher 2.

Ele segue os termos adicionais do Zalith Launcher 2 (GPLv3, seção 7):

- o nome do programa foi alterado para diferenciá-lo do original;
- o app mostra que é uma **versão modificada não oficial** na tela principal;
- os avisos de direitos autorais exibidos pelo programa foram mantidos.

## 🙏 Créditos

- **[Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)**, de MovTery e colaboradores: base de todo este projeto
- **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)**: motor de execução do jogo usado pelo Zalith
- **[Modrinth](https://modrinth.com)**: inspiração para o visual e o fluxo do app, e a API pública usada para buscar conteúdo

### Bibliotecas de código aberto

Este software usa as seguintes bibliotecas de código aberto (lista herdada do Zalith Launcher 2):

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
