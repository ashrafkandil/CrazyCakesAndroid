# Marwa Crazy Cakes — Android

A native Android (Jetpack Compose) port of the Marwa Crazy Cakes React Native app in
`CrazyCakesMobile`. It uses the same SQLite content database, media pack, copy, navigation,
quote flow, gallery/video experience, and “Sprinkles” Gemini assistant, so the two apps render the
same content and behave the same way.

## What is included

- **Six screens** — Home, About Us, Gallery, Reviews, Pricing, Contact — with the same copy,
  imagery, ordering, and responsive phone/tablet layout as the React Native app.
- **SQLite-first content** — the bundled `app/src/main/assets/crazy-cakes.db` and media pack are the
  offline source of truth. The database contains all 57 gallery images, both videos, six
  categories, testimonials, event types, social links, and 93 content slots.
- **Remote content-pack updates** — on first launch and on resume the app checks
  `MOBILE_CONTENT_URL/manifest.json`. A newer database is downloaded, size and SHA-256 verified,
  SQLite integrity checked, then activated without a new APK. Images/videos fall back to the
  bundled assets if the host is unavailable.
- **Sprinkles assistant** — floating avatar and chat panel, Gemini JSON replies, English/Arabic
  toggle, device text-to-speech, microphone recording (AAC/M4A), safe navigation actions, gallery
  category actions, and quote-form prefill.
- **Gallery** — category chips, stable random “All” ordering, 6-per-page pagination, full-screen
  image lightbox with swipe/previous/next, and Media3 video playback with bundled-file fallback.
- **Contact** — validated quote form, event-type picker, assistant quote prefill, mailto hand-off,
  email/phone/social links from the database.

## Open in Android Studio

1. Install **Android Studio** with the bundled JDK 21 (the project uses the Gradle 9.6 wrapper and
   Android Gradle Plugin 9.4.0 with its built-in Kotlin support).
2. Install **SDK Platform 36** and **Build-Tools 36.x** from SDK Manager.
3. Open this folder (`CrazyCakesAndroid`) as a project. Android Studio creates
   `local.properties` with `sdk.dir` if it is missing.
4. Add the assistant key to `local.properties` (this file is gitignored):

   ```properties
   sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
   GEMINI_API_KEY=your-restricted-gemini-key
   # Optional:
   # GEMINI_MODEL=gemini-flash-lite-latest
   # GEMINI_API_BASE_URL=https://generativelanguage.googleapis.com
   # MOBILE_CONTENT_URL=https://ashrafkandil.github.io/CrazyCakesMobile/mobile-content
   ```

   See `local.properties.example`. Without a key the app still runs; the assistant reports that it
   is not configured.
5. Select the `app` run configuration and run on an emulator or device (Android 8.0 / API 26+).

### Command line

```powershell
$env:JAVA_HOME="C:\Path\To\Android Studio\jbr"
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Configuration

| Key | Purpose | Default |
| --- | --- | --- |
| `GEMINI_API_KEY` | Gemini key for Sprinkles; embedded in the APK at build time | empty |
| `GEMINI_MODEL` | Preferred model; the app falls back through a model chain | empty |
| `GEMINI_API_BASE_URL` | Gemini-compatible API base | `https://generativelanguage.googleapis.com` |
| `MOBILE_CONTENT_URL` | Hosted content pack root | `https://ashrafkandil.github.io/CrazyCakesMobile/mobile-content` |

`GEMINI_API_KEY` and `MOBILE_CONTENT_URL` are compiled into `BuildConfig`. Use a restricted API key
and rotate it if it leaks, exactly as noted in the React Native README.

## Architecture

```text
app/src/main/java/com/marwacrazycakes/app/
  MainActivity.kt                 splash, phone/tablet navigation shell, bottom bar/sidebar
  data/
    Models.kt                     content rows
    ContentRepository.kt          bundled DB install, cache, queries, SHA-256 content updates
    QuoteDraftStore.kt            assistant -> contact form draft store
  assistant/
    AssistantModels.kt            pages, copy (EN/AR), actions, chat types
    AssistantClient.kt            Gemini REST client, system prompt, schema and validation
    AssistantOverlay.kt           floating avatar + chat panel
    AvatarFace.kt                 Compose Canvas baker avatar
    SpeechController.kt           Android TextToSpeech
    VoiceRecorder.kt              MediaRecorder AAC/M4A capture
  ui/
    theme/                        colours, fonts, Material theme
    components/Common.kt          images, watermark, buttons, footer, text fields
    screens/                      Home, About, Gallery, Reviews, Pricing, Contact
app/src/main/assets/
  crazy-cakes.db                  bundled SQLite content
  images/                         bundled media (same paths/content as the RN app)
  videos/                         bundled MP4 media
```

### Native equivalents of the React Native libraries

| React Native | Android |
| --- | --- |
| `expo-sqlite` | Android `SQLiteDatabase` |
| `expo-image` | Coil 2 `AsyncImage` with asset fallback |
| `expo-video` | AndroidX Media3 ExoPlayer |
| `expo-speech` | Android `TextToSpeech` |
| `expo-audio` | Android `MediaRecorder` + `Base64` |
| `expo-crypto` / `expo-file-system` | `MessageDigest` / `File` + `HttpURLConnection` |
| `react-native-svg` avatar | Compose `Canvas` |
| `react-native-safe-area-context` | Compose window insets |

## Content pack

The updater implements the same contract as `src/db/database.ts`:

1. Fetch `manifest.json` with a 64-character hex `version` and a `database` descriptor
   (`file`, `bytes`, `sha256`).
2. Skip when the version already matches.
3. Download `crazy-cakes.db?version=...`, verify byte count and SHA-256.
4. Run `PRAGMA integrity_check`, confirm all required tables, and cache the database as
   `mobile-content-<version>.db`.
5. Save the version in `mobile_meta`, activate the database, and notify the UI.
6. On failure, keep the last valid database and bundled media.

The content host remains `https://ashrafkandil.github.io/CrazyCakesMobile/mobile-content` and must
stay in sync with the React Native deployment.

## Validation performed

- `./gradlew assembleDebug` succeeds against AGP 9.4.0 built-in Kotlin, Gradle 9.6, JDK 21.
- `./gradlew lintDebug` completes with no errors (warnings are dependency/version suggestions).
- Installed and launched on an Android 16 / API 36 emulator, in phone and 800dp tablet layouts.
- Verified all six screens, gallery grid and pagination, image lightbox, video playback, assistant
  chat/Gemini reply, assistant navigation, quote prefill into the contact form, content-pack
  download/activation, and bundled image/video fallback with the network disabled.

## Notes

- The app is deliberately edge-to-edge; the bottom bar and dialogs consume navigation-bar insets.
- The assistant key is shipped in the APK (there is no server), matching the React Native app’s
  documented security model.
- The native port uses the React Native app’s own database and media files as the source of truth;
  update those assets (or publish a new content pack) to change content.
