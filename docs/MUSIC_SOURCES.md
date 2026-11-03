# LOQ Music Sources & Architecture

## Overview & Architecture Decision

LOQ operates under **Option A: Keeping Existing Music Sources**. The application architecture leverages a hybrid music playback model supporting both online streaming through YouTube Music and offline local audio playback from the user's Android device storage.

```
+-----------------------------------------------------------------------------------+
|                                  LOQ Application                                   |
|   (Modern Jetpack Compose UI, Navigation, Personalization, GitHub Update System)   |
+-----------------------------------------+-----------------------------------------+
                                          |
                +-------------------------+-------------------------+
                |                                                   |
                v                                                   v
+-------------------------------+                   +-------------------------------+
|     Online Music Engine       |                   |      Local Music Engine       |
|       (YouTube Music)         |                   |     (On-Device Storage)       |
+-------------------------------+                   +-------------------------------+
| - Module: :innertube          |                   | - MediaStore Audio Content    |
| - InnerTube REST Client       |                   | - LocalSongScanner            |
| - YouTube.kt / Endpoints      |                   | - SupportedLocalAudio         |
| - Models & Response Parsers   |                   | - LocalSongScreen / ViewModel |
| - YTPlayerUtils / PoTokenGen  |                   | - Room DB (LocalItem entity)  |
+---------------+---------------+                   +---------------+---------------+
                |                                                   |
                +-------------------------+-------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                           Unified Playback Architecture                           |
|                               (Module: :playback)                                 |
+-----------------------------------------------------------------------------------+
| - AndroidX Media3 (ExoPlayer 1.x) & MediaLibraryService (MusicService)           |
| - ResolvingDataSource.Factory (Dynamic URL resolution / Cache bypass)             |
| - CacheDataSource & SimpleCache (ChunkingDataSource / Download management)        |
| - DSP Audio Pipeline: Parametric Equalizer, Automix, Crossfade, Biquad Filter     |
+-----------------------------------------------------------------------------------+
```

---

## 1. Online Music Metadata

### Source & Endpoints
Online music discovery, metadata, search suggestions, artist information, albums, and playlists are fetched directly from YouTube Music using InnerTube API endpoints (`/youtubei/v1/*`).

* **Core Client**: Located in `innertube/src/main/kotlin/com/music/innertube/YouTube.kt` and `innertube/src/main/kotlin/com/music/innertube/InnerTube.kt`.
* **Endpoints Used**:
  * **Home / Explore**: `/youtubei/v1/browse` (`FEmusic_home`, `FEmusic_explore`, mood and genre categories). See `innertube/.../pages/HomePage.kt` and `BrowseResult.kt`.
  * **Search & Suggestions**: `/youtubei/v1/search` and `/youtubei/v1/music/get_search_suggestions`. See `innertube/.../pages/SearchPage.kt`, `SearchSummaryPage.kt`, and `SearchSuggestionPage.kt`.
  * **Album & Artist**: `/youtubei/v1/browse` targeting album browse IDs (`MPREb_*`) and artist channel IDs (`UC*`, `FEmusic_artist_*`). See `innertube/.../pages/AlbumPage.kt` and `ArtistPage.kt`.
  * **Playlists & Continuations**: `/youtubei/v1/browse` (`VL*` / `RDAMPL*`) and continuation tokens (`browseContinuation`). See `PlaylistPage.kt` and `Continuation.kt`.
  * **Radio / Next / Queue**: `/youtubei/v1/next` (`watchEndpoint` with videoId and playlistId). See `NextPage.kt`.

### Data Models
All responses are deserialized using Kotlinx Serialization into typed models:
* `innertube/src/main/kotlin/com/music/innertube/models/response/`: `BrowseResponse`, `SearchResponse`, `NextResponse`, `PlayerResponse`.
* Item wrappers: `SongItem`, `AlbumItem`, `ArtistItem`, `PlaylistItem`.

---

## 2. Streaming URL Resolution

Streaming URLs are dynamically resolved at playback time without storing expired URLs:

1. **Resolution Orchestration**:
   `app/src/main/kotlin/com/music/echo/utils/YTPlayerUtils.kt` coordinates stream acquisition via `playerResponseForPlayback()`.
2. **Endpoint & Clients**:
   The `/youtubei/v1/player` endpoint is invoked using configured InnerTube clients (e.g., `WEB_REMIX`, `ANDROID_VR`, `TVHTML5`, `IOS`).
3. **Decryption & Signature Handling**:
   * Signature cipher / `s` param de-scrambling is handled by `echo.music.iad1tya.utils.cipher.CipherDeobfuscator`.
   * `n`-parameter transformation is solved via `echo.music.iad1tya.utils.sabr.EjsNTransformSolver`.
   * Proof-of-Origin (PO) token generation is provided by `echo.music.iad1tya.utils.potoken.PoTokenGenerator`.
4. **Fallback Engine**:
   If direct InnerTube resolution encounters throttling or CDN blocks, `NewPipeExtractor` (`com.music.innertube.NewPipeExtractor`) acts as an automated fallback.

---

## 3. Playback Pipeline & Starting Playback

Playback is powered by Google's **AndroidX Media3 (ExoPlayer)** within a foreground `MediaSessionService`:

* **Service Entry Point**: `app/src/main/kotlin/com/music/echo/playback/MusicService.kt` extends `MediaLibraryService`.
* **Queue Systems**: Managed in `playback/src/main/kotlin/echo/music/iad1tya/playback/queues/`:
  * `YouTubeQueue`, `YouTubePlaylistQueue`, `YouTubeAlbumRadio` for streaming.
  * `ListQueue`, `LocalAlbumRadio`, `LocalMixQueue` for offline tracks.
* **Stream Feeding (`ResolvingDataSource`)**:
  In `MusicService.kt:createDataSourceFactory()`:
  * When `mediaId` is requested, `ResolvingDataSource` intercepts the request.
  * If the audio is locally downloaded or cached in `downloadCache` or `playerCache` (`SimpleCache`), it serves the file directly.
  * If remote, it queries `YTPlayerUtils.playerResponseForPlayback(videoId)` to obtain a valid HTTPS playback stream (`googlevideo.com`), wrapped in `OkHttpDataSource.Factory`.
* **Audio Processing (DSP)**:
  Signals pass through custom audio processors before reaching the hardware sink:
  * `EqualizerService` & `CustomEqualizerAudioProcessor`: Multi-band parametric EQ.
  * `AutomixDuckAudioProcessor` & `SilenceSkippingAudioProcessor`: Smooth crossfades and gapless transitions.

---

## 4. Local Music Discovery & Playback

* **Discovery**:
  `app/src/main/kotlin/com/music/echo/localmedia/LocalSongScanner.kt` queries Android's system `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`.
* **Metadata Extraction**:
  Retrieves track title, artist, album, duration, audio format, and genre.
* **Storage in Database**:
  Indexed local songs, albums, and artists are synced into SQLite via Room (`echo.music.iad1tya.db.MusicDatabase`, `SongEntity`, `LocalItem`).
* **Playback Starting**:
  When a local song is queued, its `mediaId` is encoded as a content URI string (`content://media/external/audio/media/<id>`).
  `MusicService` checks `mediaId.isLocalMediaId()`, opening the file directly via Android's `ContentResolver.openFileDescriptor()` without network interaction.

---

## 5. Libraries & APIs Involved

* **AndroidX Media3 (ExoPlayer 1.x)**: MediaSession, ExoPlayer, CacheDataSource, ResolvingDataSource.
* **OkHttp 4.x / 5.x**: Low-level HTTP requests, connection pooling, proxy support.
* **Kotlinx Serialization & Protobuf**: High-speed JSON and protocol buffer parsing.
* **Android MediaStore API**: Hardware and system storage indexing.
* **Room SQLite**: Local caching of tracks, playlists, playback history, favorites, and formats.
* **Coil 3.x**: Asynchronous image loading with memory and disk bitmap caching.

---

## 6. Upstream Implementation vs. LOQ Maintained Code

| Component / Subsystem | Upstream Origin | Maintained by LOQ |
|-----------------------|----------------|-------------------|
| **InnerTube Client** | ViMusic / InnerTune foundation | LOQ maintains endpoint updates, bug fixes, and network configs |
| **Stream Deobfuscation** | Upstream cipher & PO Token modules | LOQ maintains player client fallback and solver stability |
| **ExoPlayer Audio Engine** | AndroidX Media3 / Upstream | LOQ maintains DSP tuning, memory footprint, and low-latency cache |
| **Local Music Scanning** | MediaStore integration | LOQ maintains query optimization, folder exclusion, and tagging |
| **User Interface (UI/UX)** | Replaced completely | **100% LOQ**: Modern dark theme, lavender accents, Moon Play inspiration, rounded cards, floating navbar, redesigned player |
| **Personalization Engine** | Custom LOQ feature | **100% LOQ**: Custom welcome flows, priority rules (Bena, Yogi, Mudhu, Shahid/Hussain, Default), easter eggs |
| **Update System** | Replaced upstream updater | **100% LOQ**: Native GitHub Releases API updater with SHA-256 verification |
| **Branding & Assets** | Replaced upstream branding | **100% LOQ**: Official LOQ transparent vector/raster logo, identity, and social links |

---

## 7. Legal Ownership & Terms Notice

> [!IMPORTANT]
> **Legal Ownership Distinction:**
>
> 1. **LOQ Ownership**: LOQ owns its brand identity, name, visual designs, original source code modifications, build automation, and GitHub repository.
> 2. **YouTube & Content Ownership**: LOQ **DOES NOT** own, host, store, or license YouTube Music's catalogue, music recordings, artist trademarks, album art, or YouTube services. All audio streaming and metadata accessed through the InnerTube client are properties of Google LLC and their respective copyright holders.
> 3. **Open Source Compliance**: LOQ is distributed under the GNU General Public License v3.0 (GPLv3). All original upstream copyright notices, license texts, and attribution files are strictly preserved.

