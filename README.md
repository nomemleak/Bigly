<p align="center"><img src="fastlane/metadata/android/en-US/images/icon.png" width="96" alt="Bigly icon"></p>

# Bigly

**Show any text in huge letters across your whole screen.** · [Version française](README.fr.md)

Bigly is a small, free and open source Android app. Type a message, tap **Show**, and hold up your phone: at a concert, in a noisy room, at the airport, or to talk with someone across the street.

## Features

- **Fixed mode**: the font size adjusts itself to fill the screen. Multi-line text is supported and words are never cut.
- **Scrolling mode** (LED banner): adjustable speed, left to right or right to left, continuous loop, pause button.
- **Effects**, one at a time: blink, animated rainbow, shake, pulse, with an adjustable speed.
- **History** of the last messages (20 by default, up to 100): replay with one tap, delete one, or clear everything.
- **Colors**: text and background from a palette or any custom color, and a warning when contrast is low.
- Keeps the screen on and can raise the brightness to the maximum while a message is shown.
- Automatic orientation, with a button to lock portrait or landscape.
- **Home screen widget**: one tap shows the last message full screen.
- **Launcher shortcuts** (long press on the icon): New message, Last message, History.
- **Share to Bigly**: share text from any app; it fills the input field, ready to show.
- **Emojis** at full size in every mode and effect; the automatic sizing measures them too.
- **Export as image**: a PNG of the message as displayed, shared through the Android share menu.
- **13 color presets** (yellow on black, green on black, white on blue…), all with a contrast of at least 4.5:1, available in the settings and on the display screen.
- Material You colors on Android 12+, with a clean fallback theme on Android 8 to 11. Light, dark or system theme.
- English and French, following the phone language.

Tap the message to show or hide the controls (back, effect, colors, orientation lock, export, pause). The system back gesture leaves full screen.

## Privacy

No permissions (exported images are shared from the app's private cache through a FileProvider, so no storage permission is needed), no network access, no ads, no trackers, no proprietary libraries. Settings and history are stored on the device only, and are excluded from cloud backups.

## Accessibility

- Animations follow the system "remove animations" setting: effects stay still when it is on.
- Blinking never goes above 2.5 flashes per second.
- All controls have content descriptions; the settings show a warning when the text and background colors lack contrast.

## Build

Requirements: JDK 17 or newer, and the Android SDK (with `ANDROID_HOME` set, or a `local.properties` file containing `sdk.dir=...`). Android Studio provides both.

```sh
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # debug APK
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it with:

```sh
adb install app/build/outputs/apk/debug/app-debug.apk
```

Every push to `main` is also built by GitHub Actions. The latest debug APK is attached to the [`debug-latest` pre-release](../../releases/tag/debug-latest), and to each run in the **Actions** tab as the `bigly-debug-apk` artifact.

| | |
|---|---|
| Language | Kotlin, Jetpack Compose, Material 3 |
| Android | 8.0+ (minSdk 26), targets API 36 |
| Application id | `app.bigly` (debug: `app.bigly.debug`) |

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how the code is organized. Store metadata for F-Droid lives in `fastlane/metadata/android`.

## License

Copyright (C) 2026 Bigly contributors.

Bigly is free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](LICENSE) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

---

AI assistance was used, in an ethical and supervised way, to help write this app; every decision and the final result remain under human responsibility.
