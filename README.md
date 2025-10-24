# GetOnUp

A minimalist workout logbook and performance tracker built with Jetpack Compose and Material Design 3 Expressive.

## Project at a glance

- **Package name:** `com.silvzr.getonup`
- **UI toolkit:** Jetpack Compose with Material 3 Expressive styling
- **Minimum SDK:** 24 (Android 7.0)
- **Debug build:** Shrunk and minified via `assembleDebug` with a consistent, repository-stored debug keystore.

## Getting started

Make sure you have Android Studio Giraffe or newer (Electric Eel+) with the latest Android SDK and build tools installed.

```powershell
cd d:\GitHub\GetOnUp
.\gradlew.bat assembleDebug
```

The resulting APK can be found under `app/build/outputs/apk/debug/`.

## UI roadmap

- [x] Expressive top toolbar with timeline, calendar, and workout entry actions
- [ ] Wire up navigation destinations for the toolbar actions
- [ ] Add workout logging flows and historical performance charts

Material guidance and expressive component patterns are referenced from [m3.material.io](https://m3.material.io/components).

