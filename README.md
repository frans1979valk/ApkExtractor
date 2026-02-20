# APK Extractor

A simple, friendly, ad-free APK extractor and backup app for Android.
No subscriptions, no trackers, no internet permission.

## How to Open and Build

1. Open Android Studio (Iguana 2024.1+ recommended)
2. Select **File > Open** and choose the `ApkExtractor` folder
3. Android Studio will sync Gradle automatically
4. If prompted about the Gradle wrapper, click **OK** to let Android Studio generate `gradle-wrapper.jar`
5. Connect a device or start an emulator (API 26+)
6. Click **Run** (green play button)

### Command-line build (optional)

If you want to build from the terminal and the wrapper jar is missing:

```bash
# Generate the wrapper jar (requires Gradle installed)
gradle wrapper --gradle-version 8.7

# Then build
./gradlew assembleDebug
```

## Features

- **Installed Apps List**: Browse all installed apps with search and sort
- **App Details**: View version, package name, update date, and APK size
- **Export APK**: Save APK to any location using Android's file picker (SAF)
- **Share APK**: Share APK via the system share sheet (Quick Share, messaging apps, etc.)
- **Safe Uninstall**: Remove user-installed apps via the system uninstaller (system apps are protected)
- **Settings**: Toggle system apps visibility, change sort order
- **About/Help**: Privacy statement and usage guide

## How Export Works (SAF)

Export uses Android's **Storage Access Framework** (ACTION_CREATE_DOCUMENT):

1. Tap an app in the list to open its details
2. Tap **Export APK**
3. The system file picker opens with a suggested filename
4. Choose where to save the APK
5. The app stream-copies the APK from its install location to your chosen destination

This approach is fully Scoped Storage compliant and requires no storage permissions.

## How Share Works

Share copies the APK to a temporary cache directory, then uses `FileProvider` to share it via `ACTION_SEND`:

- Opens the native Android share sheet
- Works with Quick Share / Nearby Share, messaging apps, file transfer apps, etc.
- No Bluetooth or Wi-Fi permissions needed — the system handles transport
- Cached files are automatically cleaned up after 24 hours

## How Uninstall Works

- **User apps**: Tapping "Remove" opens the standard system uninstaller (ACTION_DELETE). No silent uninstalls.
- **System apps**: The Remove button is hidden. A "System app" badge is shown, and tapping it explains why removal isn't possible and offers to open system App Info.

## System Apps Limitations

- System apps (pre-installed by the manufacturer) are shown with a "System app" badge
- Some system APKs may not be readable on certain devices due to OS restrictions
- System apps cannot be uninstalled through this or any third-party app
- Export and share should work for most system apps, but results vary by device

## Privacy & Compliance

- **No internet permission** — the app cannot access the network
- **No analytics or trackers** — nothing is collected or sent
- **No ads or billing** — completely free
- **No DRM bypass** — only exports APKs from already-installed apps
- **Play Store compliant** — uses QUERY_ALL_PACKAGES with legitimate justification

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- MVVM (ViewModel + Repository)
- Coroutines
- DataStore Preferences
- Navigation Compose
- minSdk 26, targetSdk 35

## Supported Languages

- English (en)
- Dutch (nl)
- German (de)
- Hindi (hi)
- Spanish (es)
- French (fr)

Language follows the device system language automatically.
