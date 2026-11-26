# YT Fumi

**YT Fumi** is a modern Android application written in **Java** designed to download videos from YouTube and other supported sites as **MP4** video or extract audio as **MP3** using **yt-dlp**. The app includes an in-app engine manager/updater for keeping `yt-dlp` up to date, and a responsive UI that adapts to both phones and tablets in portrait and landscape orientations.

## Features

- **Download media** from a user-provided URL as:
  - **MP4 Video** with quality options: `Best`, `1080p`, `720p`, `480p`
  - **MP3 Audio** with quality options: `320 kbps`, `192 kbps`, `128 kbps`
- **Real-time download progress** showing percentage, status messages, and completion/error states
- **In-app Engine Manager** to initialize and update `yt-dlp` on demand
- **Recent downloads list** showing recent download files
- **Paste from clipboard** button for URL input
- **Fully responsive UI** using Material Components and ConstraintLayout, scaling naturally on phones and tablets via `dp` dimensions and `values-sw600dp` resource overrides

## Architecture

The project follows clean code principles with separated responsibilities:

| File | Responsibility |
|------|---------------|
| `MainActivity.java` | UI setup, tab handling, event listeners |
| `DownloadHelper.java` | Download execution with format mapping and yt-dlp integration |
| `EngineUpdater.java` | Engine initialization and update management |
| `RecentItem.java` | Data model for recent downloads |
| `DownloadAdapter.java` | RecyclerView adapter for recent downloads list |
| `activity_main.xml` | Main UI layout with TabLayout and two tabs |
| `item_download.xml` | Layout for each recent download item |

## UI Layout

The app uses a **TabLayout** with two tabs:

- **Download tab**: URL input, Paste button, Format toggle (MP4/MP3), Quality spinner, Download button, Progress bar, Status text, Recent downloads list
- **Engines tab**: Engine status, Initialize/Update buttons, Engine log output

Quality options dynamically change when toggling between MP4 Video and MP3 Audio formats.

## Project Structure

```
YT_Fumi/
├── settings.gradle
├── build.gradle
├── gradlew, gradlew.bat
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/ytfumi/app/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── DownloadHelper.java
│   │   │   │   ├── EngineUpdater.java
│   │   │   │   ├── RecentItem.java
│   │   │   │   └── DownloadAdapter.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   └── item_download.xml
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml
│   │   │   │       ├── colors.xml
│   │   │   │       ├── themes.xml
│   │   │   │       ├── dimens.xml
│   │   │   │       └── values-sw600dp/dimens.xml
│   │   │   └── res/drawable/ (auto-generated)
│   │   └── debug/
│   └── ...
└── .github/
    └── workflows/
        └── build-apk.yml
```

## Building from Command Line (No Android Studio Required)

### Prerequisites

- **Git** (already installed)
- **OpenJDK 17** (for GitHub Actions; not needed locally if using Gradle wrapper with Android NDK)
- **GitHub Actions** for cloud builds (recommended)

### Local Build (requires Android NDK and SDK)

```bash
# 1. Clone the repository
git clone https://github.com/Asaduddeenamr2006/YT_Fumi.git
cd YT_Fumi

# 2. Ensure Gradle wrapper is executable
chmod +x gradlew

# 3. Build the debug APK
./gradlew assembleDebug
```

The generated APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions Build

The project includes a GitHub Actions workflow at `.github/workflows/build-apk.yml` that builds the APK on `ubuntu-latest` with JDK 17. After pushing to GitHub:

1. Go to the **Actions** tab in your GitHub repository
2. Select the "Build YT Fumi APK" workflow
3. Click "Run workflow"
4. Download the generated APK from the artifact after the workflow completes

## Dependencies

The app uses the following key dependencies:

| Dependency | Purpose |
|------------|---------|
| `com.google.android.material:material:1.6.1` | Material Design UI components |
| `androidx.constraintlayout:constraintlayout:2.1.4` | Responsive layout management |
| `io.github.junkfood02.youtubedl-android:library:0.18.1` | yt-dlp Android wrapper library |
| `io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1` | FFmpeg support for audio extraction |

The yt-dlp library is obtained from **JitPack** (`maven { url 'jitpack.io' }`) and **Maven Central** (`mavenCentral()`).

## Format Mapping

The app maps UI quality selections to yt-dlp format selectors:

### MP4 Video Qualities
- `Best` → `bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best`
- `1080p` → `bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[height<=1080]/best`
- `720p` → `bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720]/best`
- `480p` → `bestvideo[height<=480][ext=mp4]+bestaudio[ext=m4a]/best[height<=480]/best`

### MP3 Audio Qualities
- `320 kbps` → `-x --audio-format mp3 --audio-quality 320K`
- `192 kbps` → `-x --audio-format mp3 --audio-quality 192K`
- `128 kbps` → `-x --audio-format mp3 --audio-quality 128K`

## Storage

Files are saved to the app's private external files directory:
```
<app_documents_dir>/Download/ytfumi/<title>.<ext>
```

This approach works on all Android versions (including Android 10+ Scoped Storage) without requiring runtime storage permissions for write access. The `INTERNET` permission is required for downloading.

## Updating yt-dlp

Use the **Engines tab** in the app to:
- **Initialize Engine**: Download and set up the bundled `yt-dlp` binary
- **Update Engine**: Check for and install the latest `yt-dlp` version from the GitHub releases channel

## License

GPL-3.0 license - see the repository for full terms.