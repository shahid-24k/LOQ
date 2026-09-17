# Phase 4 — Self-Updater Flavor Gating

STATUS: COMPLETE / NO CODE CHANGE REQUIRED

## Corrected understanding (do not reintroduce the earlier assumption)

In this codebase, "foss" and "gms" describe *whether Google Play Services
client libraries are bundled* (Cast, Firebase Analytics/Crashlytics) --
nothing about distribution channel:

    // app/build.gradle.kts
    create("foss") { // FOSS variant (default) - F-Droid compatible, no Google Play Services
    create("gms")  { // GMS variant - with Google Cast support (requires Google Play Services)

An earlier audit pass (round 2 of this project) incorrectly assumed "gms"
meant "Play Store distribution" and flagged the self-updater as a Play
policy risk that needed to be removed from the gms flavor. That assumption
was never verified against the actual manifests and was wrong.

The real, already-correct arrangement:
- app/src/foss/AndroidManifest.xml already removes REQUEST_INSTALL_PACKAGES
  via tools:node="remove" -- because F-Droid's inclusion policy prohibits
  any self-update capability, and "foss" is the F-Droid-targeting flavor.
- "gms" has no manifest override and retains the permission and the full
  in-app updater (UpdateScreen, UpdateDownloadWorker, install-intent code)
  -- this is the direct-download build (GitHub releases / echomusic.fun),
  not a Play-Store build. Nothing in the repo indicates gms is Play-Store
  distributed.

## Decision

No code change made. The existing foss/gms updater arrangement is correct
as-is and must not be "fixed" based on the old (wrong) assumption:
- foss -> REQUEST_INSTALL_PACKAGES removed, no self-update (correct, keep)
- gms  -> existing updater behavior retained (correct, keep)

Explicitly NOT done, and should not be added later on the basis of the
retracted assumption:
- UpdaterAvailability.kt (flavor-gated feature flag) -- not created
- app/src/gms/AndroidManifest.xml -- not created
- Any runtime/compile-time flavor check disabling the updater for gms
- Any updater source-set restructuring

## Deferred, unrelated cleanup

EchoNotificationProvider.kt remains in
app/src/main/kotlin/com/music/echo/echomusic/updater/downloadmanager/,
despite being real Media3 playback-notification infrastructure unrelated
to the updater. Relocating it to the playback package is a legitimate but
separate, isolated cleanup -- not required for Hlbk functionality, not
part of this phase, and not done here.
