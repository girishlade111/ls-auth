# LS Auth

**Enterprise-grade, local-only TOTP/HOTP authenticator for Android.**

LS Auth is a privacy-first two-factor authentication (2FA) app that generates time-based (TOTP, RFC 6238) and counter-based (HOTP, RFC 4226) one-time passwords entirely **on-device**. Your secrets never leave your phone — there is no cloud sync, no telemetry, and no server-side dependency for code generation.

Built with a modern Android stack (Kotlin + Jetpack Compose + Material 3), LS Auth combines a full-featured authenticator with a secure password vault, biometric app-lock, QR-code import/export, folder organization, and a wearable companion screen — all secured by the Android Keystore.

---

## Table of Contents

- [Features](#features)
- [Security Model](#security-model)
- [Screens](#screens)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Build & Install](#build--install)
  - [Google Drive Backup Setup](#google-drive-backup-setup-optional)
- [Configuration](#configuration)
  - [Signing](#signing)
  - [Secrets (.env)](#secrets-env)
- [Testing](#testing)
- [Import / Export Compatibility](#import--export-compatibility)
- [License](#license)

---

## Features

### One-Time Passwords (OTP)
- **TOTP** (RFC 6238) with configurable time step and digits.
- **HOTP** (RFC 4226) counter-based codes with manual counter increments.
- **Multiple hash algorithms**: HMAC-SHA1, HMAC-SHA256, HMAC-SHA512.
- **Pure, local code engine** (`TotpEngine`) — Base32 decoding, HMAC computation, and dynamic truncation implemented from scratch with zero external state or network calls.
- **Live countdown ring** per code with progress indication and automatic refresh.
- **Time sync checker** (`TimeSyncChecker`) to detect clock drift that would invalidate TOTP codes.

### Account Management
- Add accounts via **QR code scanning** (CameraX + ZXing) or **manual entry**.
- Organize accounts into **folders**.
- **Service icons** with brand vectors and auto-generated monogram avatars.
- Search, filter, and folder-based browsing on the home screen.
- Account detail view with full metadata (issuer, secret, algorithm, digits, period/counter).

### Import / Export
- **Universal import/export parser** supporting common `otpauth://` URI formats.
- **Google Authenticator migration import** (`.txt` export format) with a staged review screen before committing accounts.
- **Encrypted exports** protected by the Android Keystore.
- Clipboard auto-clear with a countdown toast banner after copying a code.

### Security
- **Android Keystore encryption** (`KeystoreEncryption`) for all stored secrets at rest.
- **App lock** with configurable inactivity timeout (auto-lock when the app backgrounds).
- **Biometric unlock** (Fingerprint/Face) via AndroidX Biometric.
- **FLAG_SECURE** screen-capture protection.
- **Local-only design**: no servers, no analytics, no network requirement for OTP generation.
- Password vault stored in Room, encrypted at rest.

### Extras
- **Password vault** — store and manage site/app credentials with a built-in **password generator**.
- **Passwordless login screen** (passkey/credential-manager oriented UI).
- **Wearable companion screen** — displays codes in a watch-friendly layout.
- **Theme modes**: Light / Dark / System, with a custom Material 3 design system.
- **Haptics** feedback (`HapticManager`) on key interactions.
- **Onboarding flow** for first-run setup.

---

## Security Model

1. **Everything is local.** OTP generation, parsing, and code computation happen entirely on-device. The only permissions requested are `CAMERA` (for QR scanning) and `INTERNET` (used only for the *optional* Google Drive backup feature).
2. **Encryption at rest.** Secrets and vault entries are encrypted using keys stored in the **Android Keystore** — hardware-backed where available — never in plaintext on disk.
3. **App lock.** The app re-locks on backgrounding and after a configurable inactivity timeout. Unlock requires the device credential or biometrics.
4. **No clipboard leakage.** Copied codes are automatically cleared from the clipboard after a short countdown.
5. **No FLAG_SECURE bypass in release builds** — screen recording/screenshot protection is applied via `AppLockManager`.

---

## Screens

| Screen | Purpose |
|---|---|
| `OnboardingScreen` | First-run setup & feature intro |
| `HomeScreen` | Search, folder chips, account list overview |
| `CodeListScreen` | All codes with live countdown rings |
| `AccountDetailScreen` | Full account metadata & actions |
| `QrScannerScreen` | Camera-based QR capture |
| `ManualEntryScreen` | Manually enter issuer/secret/algorithm |
| `MigrationReviewScreen` | Review imported accounts before committing |
| `ImportExportScreen` | Encrypted export / universal import |
| `PasswordVaultScreen` | Stored credentials + password generator |
| `PasswordlessLoginScreen` | Passkey-style login flow |
| `SettingsScreen` | Theme, lock timeout, backup, preferences |
| `LockScreen` | Biometric/device-credential unlock gate |
| `WearableCompanionScreen` | Watch-friendly code display |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.09.00), Material 3, Navigation Compose |
| Async | Kotlin Coroutines |
| Database | Room 2.7.0 (KSP codegen) |
| Persistence | DataStore Preferences (settings) |
| Camera | CameraX 1.5.0 (Camera2 + lifecycle + view) |
| QR decoding | ZXing 3.5.3 |
| Security | Android Keystore, AndroidX Biometric 1.1.0 |
| Images | Coil 2.7.0 |
| Backup (optional) | Google Drive / Google Play Services |
| Build | AGP 9.1.1, Gradle toolchains, Secrets Gradle Plugin |
| Testing | JUnit 4, Robolectric, Roborazzi (screenshot), Espresso, Compose UI tests |

**SDK levels:** `minSdk 24` (Android 7.0) · `targetSdk 36` · `compileSdk 36`

---

## Project Structure

```
ls-auth/
├── app/
│   ├── src/
│   │   ├── main/java/com/example/
│   │   │   ├── MainActivity.kt          # Root activity, nav graph, lifecycle lock
│   │   │   ├── OtpEngine.kt             # (re-export) OTP engine entry point
│   │   │   ├── crypto/                  # TOTP/HOTP, otpauth URI parser,
│   │   │   │                            # migration parser, Base32, Keystore encryption
│   │   │   ├── data/                    # Room DB, DAOs, entities, repositories,
│   │   │   │                            # Google Drive backup
│   │   │   ├── model/                   # Account, Folder, icon pack models
│   │   │   ├── security/                # App lock & secure-flag manager
│   │   │   ├── ui/
│   │   │   │   ├── MainViewModel.kt     # Shared view model
│   │   │   │   ├── components/          # Countdown ring, code cards, dialogs...
│   │   │   │   ├── screens/             # All screens (see table above)
│   │   │   │   └── theme/               # Material 3 theme, colors, typography
│   │   │   └── util/                    # Time sync, generator, haptics, clipboard
│   │   ├── test/                        # Unit + Robolectric + Roborazzi tests
│   │   └── androidTest/                 # Instrumented tests
│   ├── build.gradle.kts
│   └── AndroidManifest.xml
├── assets/                              # Non-code assets (AI Studio metadata)
├── gradle/
│   ├── libs.versions.toml               # Central version catalog
│   └── wrapper/
├── build.gradle.kts                     # Root build config
├── settings.gradle.kts
├── gradle.properties
├── metadata.json                        # AI Studio app metadata
└── .env.example                         # Secrets template
```

---

## Getting Started

### Prerequisites

- **Android Studio** (latest stable, with Android SDK Platform 36)
- **JDK 17+** (foojay toolchain resolver handles provisioning)
- An Android device/emulator running **Android 7.0 (API 24)** or newer

### Build & Install

```bash
# Clone the repository
git clone https://github.com/girishlade111/ls-auth.git
cd ls-auth

# Debug build
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

> **Windows users:** use `gradlew.bat` instead of `./gradlew`.

### Google Drive Backup Setup (Optional)

The app supports encrypted backup to Google Drive via the `GoogleDriveBackupRepository`. To enable it:

1. Create a Firebase project and add your app's SHA-1 to the **App Check** configuration.
2. Place `google-services.json` in `app/`.
3. Add your Gemini API key to the Secrets panel (see below) if you use server-side Gemini features.

The build is configured to tolerate a missing `google-services.json` (`WARN` strategy), so the app compiles without it — backup simply stays disabled.

---

## Configuration

### Signing

Signing config is driven by environment variables (or falls back to a local keystore):

| Variable | Fallback | Purpose |
|---|---|---|
| `KEYSTORE_PATH` | `<rootDir>/my-upload-key.jks` | Release keystore location |
| `STORE_PASSWORD` | — | Keystore password |
| `KEY_PASSWORD` | — | Key password |

Debug builds use `<rootDir>/debug.keystore` (password `android`).

### Secrets (.env)

Secrets are managed with the **Google Secrets Gradle Plugin**:

| File | Purpose |
|---|---|
| `.env.example` | Committed template (defaults) |
| `.env` | Local, gitignored secrets |

```ini
# GEMINI_API_KEY: Required for Gemini AI API calls.
# Uncomment to package the key into the APK.
# GEMINI_API_KEY=MY_GEMINI_API_KEY
```

The `FIREBASE_APPCHECK_DEBUG_TOKEN` entry is ignored by the plugin for debug builds.

---

## Testing

The project ships with a layered test suite:

```bash
# Unit tests (JVM) — pure logic, crypto, parsers
./gradlew testDebugUnitTest

# Instrumented tests (device/emulator)
./gradlew connectedDebugAndroidTest

# Screenshot tests (Roborazzi — JVM-based, no emulator needed)
./gradlew recordRoborazziDebug   # generate golden images
./gradlew verifyRoborazziDebug   # compare against goldens
```

Coverage highlights:
- **`TotpEngine`** — RFC-compliant vector tests (SHA1/SHA256/SHA512, dynamic truncation, Base32 edge cases).
- **`OtpUriParser` / `GoogleAuthMigrationParser`** — malformed URI handling, migration `.txt` parsing.
- **UI** — Robolectric + Roborazzi screenshot tests for key screens (see `GreetingScreenshotTest`).

---

## Import / Export Compatibility

| Source | Format | Support |
|---|---|---|
| LS Auth export | Encrypted (Keystore) | ✅ Full |
| Google Authenticator | `.txt` migration export | ✅ Full (staged review) |
| Generic 2FA apps | `otpauth://` URI / QR | ✅ `totp` & `hotp`, all hashes |

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details (if present).

---

*Built with ❤️ for privacy-first security. Your codes stay yours.*
