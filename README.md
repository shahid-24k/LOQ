<div align="center">
  <img src="app/src/main/res/drawable-nodpi/loq_logo_white_transparent.png" alt="LOQ Logo" width="120"/>

  <h1>LOQ</h1>

  <p><b>A modern Android music player with YouTube Music integration and local music playback.</b></p>

  <p>
    <a href="https://instagram.com/shahid_24k"><img src="https://img.shields.io/badge/Instagram-@shahid__24k-E4405F?style=for-the-badge&logo=instagram&logoColor=white" alt="Instagram"/></a>
    <a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge" alt="License: GPLv3"/></a>
    <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform"/>
  </p>
</div>

---

## Overview

**LOQ** is a modern, responsive, and privacy-conscious Android music player crafted with Jetpack Compose. It seamlessly unifies your local audio files with online streaming powered by YouTube Music, wrapped in an immersive dark aesthetic featuring lavender accents, smooth transitions, and dedicated performance profiles designed to run flawlessly across devices—including entry-level phones with 2 GB and 3 GB of RAM.

---

## Highlights & Features

- **Dual Music Engine**: Play tracks, albums, and playlists from YouTube Music or play your offline audio collection stored on device.
- **Modern LOQ Design Language**: Immersive dark surfaces, soft lilac/lavender accents, rounded cards, floating bottom navigation, and refined mini and full players.
- **Adaptive Performance Profiles**:
  - **Lite Mode**: Conservative image sizing, lightweight static effects, and optimized memory usage tailored for 2 GB / 3 GB RAM devices.
  - **Balanced Mode**: Smooth transitions with controlled background work.
  - **Full Mode**: Premium effects and full visual polish for high-end devices.
  - Configurable anytime via **Settings → Performance Mode**.
- **Media3 & ExoPlayer Engine**: Seamless audio streaming, dynamic caching, equalizer support, and background playback with system media controls.
- **Real-Time Synchronized Lyrics**: Time-synced and plain text lyrics with multi-provider fallbacks (LRCLIB, Kugou).
- **Offline Downloads**: High-speed downloads for tracks, albums, and playlists with local storage management.
- **In-App GitHub Updates**: Directly checks official public releases from GitHub, verifies APK signatures and SHA-256 checksums, and prompts the standard Android package installer safely.
- **First-Launch Personalization**: Simple, offline name onboarding with personalized time-based greetings on the Home screen ("Good morning", "Good afternoon", "Good evening").

---

## Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Home</b><br><img src="Screenshots/sc_1.png" width="200" alt="Home Screen"/></td>
      <td align="center"><b>Player</b><br><img src="Screenshots/sc_2.png" width="200" alt="Player Screen"/></td>
      <td align="center"><b>Lyrics</b><br><img src="Screenshots/sc_3.png" width="200" alt="Lyrics"/></td>
    </tr>
    <tr>
      <td align="center"><b>Explore</b><br><img src="Screenshots/sc_4.png" width="200" alt="Explore"/></td>
      <td align="center"><b>Library</b><br><img src="Screenshots/sc_5.png" width="200" alt="Library"/></td>
      <td align="center"><b>Recognition</b><br><img src="Screenshots/sc_6.png" width="200" alt="Recognition"/></td>
    </tr>
  </table>
</div>

---

## Music Sources & Architecture

For detailed information about LOQ's streaming pipeline, stream URL resolution, local media discovery, and upstream component separation, please read:

👉 **[docs/MUSIC_SOURCES.md](docs/MUSIC_SOURCES.md)**

### Key Clarification
- **LOQ owns its branding, application source code, and original modifications.**
- LOQ **does NOT** own YouTube Music's catalogue, audio recordings, or YouTube's services.
- Online streaming and metadata are queried directly via the InnerTube API using public endpoints.
- No ads, tracking SDKs, or sponsor integrations are included.

---

## Building from Source

### Prerequisites
- **JDK 21** (Eclipse Adoptium Temurin or compatible)
- **Android SDK** with compileSdk 36, build-tools 36.x, minSdk 26
- **Gradle 9.x** (wrapper provided)

### Build Commands

To build the universal release APK:
```bash
# Set release signing variables in local.properties or environment
./gradlew :app:assembleUniversalFossRelease
```

The compiled release APK will be located at:
```
app/build/outputs/apk/universalFoss/release/app-universalFoss-release.apk
```

To run unit tests:
```bash
./gradlew :app:testUniversalFossDebugUnitTest
```

---

## Release Automation

Automated builds and GitHub Releases are powered by `.github/workflows/release.yml`. When a release tag (e.g., `v1.2.6`) is pushed, GitHub Actions automatically:
1. Restores the release keystore securely from repository secrets (`LOQ_KEYSTORE_BASE64`, `LOQ_STORE_PASSWORD`, `LOQ_KEY_ALIAS`, `LOQ_KEY_PASSWORD`).
2. Builds the `universalFossRelease` APK.
3. Computes the SHA-256 checksum.
4. Verifies APK signing via `apksigner`.
5. Publishes a GitHub Release with assets attached.

---

## Security & Privacy

- **No Remote Telemetry**: LOQ respects your privacy and contains no tracking libraries or analytics trackers.
- **Signed Official Releases**: Update downloads verify matching cryptographic signing certificates before install.
- **Zero Committed Secrets**: Keystores, passwords, and tokens are strictly excluded from source control.

---

## Attribution & Licensing

LOQ is licensed under the **GNU General Public License v3.0 (GPLv3)**.

- **LOQ Brand & Modifications**: Copyright (C) 2026 Shahid Khan ([@shahid_24k](https://instagram.com/shahid_24k)).
- **Upstream Foundations**: LOQ is derived from **Echo Music** (by Aditya) and **InnerTube** (by z-huang), licensed under GPLv3.
- All original copyrights, license headers, and attributions from upstream contributors remain fully preserved in accordance with the GPLv3 license terms.
