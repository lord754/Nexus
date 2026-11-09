# Nexus Launcher

<p align="center">
  <img alt="Nexus Launcher" src="https://img.shields.io/badge/Android-Minecraft%20Java%20Edition-3DDC84?logo=android&logoColor=white" />
  <img alt="Built with Java" src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" />
  <img alt="Built with Kotlin" src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="Native" src="https://img.shields.io/badge/C%2FC%2B%2B-CMake-00599C?logo=c%2B%2B&logoColor=white" />
  <img alt="License" src="https://img.shields.io/badge/License-Open%20Source-34A853" />
</p>

Nexus Launcher is a sleek Android launcher for Minecraft: Java Edition, designed for a modern, dark interface and a smooth mobile gaming experience. Built around the FCL engine ecosystem, it brings together launcher functionality, Java runtime handling, native support, and mod platform integrations in one Android app.

This project is an unofficial custom launcher fork and is built for players who want a more polished launcher experience on Android devices.

## Highlights

- Modern dark UI designed for mobile devices
- Minecraft Java Edition launcher support
- Built on the FCL launcher foundation
- Modrinth and CurseForge download support
- Java runtime and native runtime packaging
- Multi-architecture support for Android devices
- Modular project structure for easier maintenance and extension

## Features

- Custom launcher experience with a refined Nexus dark theme
- Game version and runtime management
- Mod and modpack integration support
- Native game execution support via Android and LWJGL tooling
- Flexible build configuration for debug/release targets
- Architecture-aware packaging for multiple Android ABIs

## Tech Stack

- Java and Kotlin
- Android app development with Gradle and Android Gradle Plugin
- C / C++ / CMake for native integration
- FCL-based launcher engine architecture
- LWJGL runtime support for Java-based Minecraft environments

## Project Structure

```text
Nexus/
├── FCL/                 # Main launcher application and UI logic
├── LWJGL/               # LWJGL build/module setup
├── Terracotta/          # Additional project component
├── ZipFileSystem/       # ZIP filesystem support
├── config/              # Configuration and checks
├── docs/                # Project documentation and architecture notes
├── gradle/              # Gradle wrapper config
├── scripts/             # Utility or build scripts
├── build.gradle.kts     # Root Gradle build config
├── settings.gradle.kts  # Gradle project settings
├── gradlew              # Unix build entrypoint
├── gradlew.bat          # Windows build entrypoint
├── LICENSE              # Project licensing
├── README.md            # Project overview
├── CHANGELOG.md         # Release history
├── .gitignore           # Git ignore rules
└── ...
```

## Why This Project Matters

Nexus is created for players who want a launcher experience tailored specifically for mobile Android devices. Instead of relying on a generic or overly limited setup, it aims to provide a cleaner experience while still using the ecosystem and runtime architecture that make Java Edition launchers work reliably.

The codebase reflects a modular architecture with a main app layer and supporting components such as FCL, LWJGL, Terracotta, and ZipFileSystem. That makes it easier to evolve the launcher, support new runtime scenarios, and keep logic separated for different responsibilities.

## Getting Started

### Prerequisites

Before building the project, make sure you have the following installed:

- Android Studio
- JDK 17
- Android SDK with required platform tools
- Gradle wrapper support via the included project files

### Clone

```bash
git clone https://github.com/lord754/Nexus.git
cd Nexus
```

### Build Debug APK

```bash
./gradlew :FCL:assembleDebug
```

Or on Windows:

```powershell
gradlew.bat :FCL:assembleDebug
```

### Build Release APK

```bash
./gradlew :FCL:assembleRelease
```

### Architecture-Specific Builds

This project supports architecture-aware builds for Android targets such as:

- arm
- arm64
- x86
- x86_64

Example:

```bash
./gradlew -Darch=arm64 :FCL:assembleDebug
```

## Build Notes

- The project uses Gradle Kotlin DSL.
- Java source compatibility is set to Java 17.
- Android signing and debug/release config are defined in the app module.
- Native assets are packaged with CMake and Android-specific runtime handling.

## Development Notes

The repository includes the following high-value areas for contributors and maintainers:

- `FCL/` — launcher UI and application logic
- `LWJGL/` — native runtime packaging support
- `docs/` — architecture and implementation notes
- `config/` — style and validation rules
- `scripts/` — project utilities and automation

## Contributing

Contributions are welcome. If you want to improve the app, fix issues, or add new functionality:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test the relevant module or build target
5. Open a pull request with a clear description

## Credits

This project builds on the work of several open-source communities and launcher-related projects, especially the FCL ecosystem and related Java Edition launcher tooling.

This is an unofficial fork, created for Android-focused launcher use and experimentation.

## License

This project is distributed under the terms of the repository's LICENSE file. Please review it before redistributing or modifying the project.

## Support and Community

For issues, discussion, feature requests, or questions, use the GitHub Issues section of this repository.

---

<p align="center">
  <strong>Built for Android Minecraft enthusiasts.</strong>
</p>
