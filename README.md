# ZapWell iOS spike

Technical test only: can a Kotlin + Compose Multiplatform screen with a native VLCKit player be built on a cloud Mac
and play IPTV-style streams (live MPEG-TS over http, HLS) on iPad and iPhone? No content, no playlists, no secrets.

- `shared/` Kotlin Compose screen (iOS targets), exported as `Shared.xcframework`.
- `iosApp/` Swift host app and the VLCKit player (`VlcPlayer.swift`).
- `project.yml` XcodeGen spec, `Podfile` for MobileVLCKit (LGPL 2.1).
- `.github/workflows/ios.yml` builds, runs on an iPad simulator (screenshots, video, status log) and makes an
  unsigned `.ipa` for sideloading.

Webotion Digital Solutions.
