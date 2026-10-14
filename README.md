# LocalSaver for Android

A standalone personal video downloader. Share a video link from Facebook, Instagram, or YouTube, or paste it into LocalSaver. The download runs on the phone and saves an MP4 to **Movies/LocalSaver**. No desktop server is needed.

## Install the APK

1. Download [LocalSaver-Android.apk from the GitHub release](https://github.com/valdzjm/local-saver-android/releases/download/v1.0.0/LocalSaver-Android.apk), or copy your local `release/LocalSaver-Android.apk` to your Android phone (USB, cloud drive, or another file-transfer method).
2. Open it in your phone's Files app. If asked, allow **Install unknown apps** for that Files/browser app, then tap **Install**. You can switch this permission off again afterward.
3. Open **LocalSaver**. Android 10 or newer is required; the APK includes ARM64 and ARMv7 builds.
4. Allow notifications when asked if you want download progress while using other apps.

This is a privately signed APK, not a Play Store release. Keep the existing installation/signing key for future compatible updates.

## Download from your social apps

1. Open the individual video, reel, or Short in Facebook, Instagram, or YouTube.
2. Tap **Share**, then **More** or the Android share menu, and choose **LocalSaver**. If the app only offers **Copy link**, copy it, open LocalSaver, and tap **Paste link**.
3. Tap **Download video**. Initial setup may take longer while the bundled video engine is unpacked and updated.
4. Wait for **Video saved**. Open your Gallery/Google Photos/Files app and look in **Movies/LocalSaver**, or use **Play** in LocalSaver's history.

Downloads continue through a foreground service when you switch apps. **Cancel download** stops the active job. History offers **Redownload**, **Play**, **Share video**, and **Open source**. Removing history does not delete the saved MP4. Redownload resolves the original page again, so it still requires an available video and any necessary sign-in.

## Videos and stories that require sign-in

1. Paste/share the video link and tap **Open browser**.
2. Sign in on the official website inside LocalSaver's browser and open/play the desired video.
3. Enable **Use this browser's signed-in session** and tap **Download this video**.

Your Facebook/Instagram/YouTube Android app and Chrome login sessions are separate from this browser. LocalSaver does not read their private data. Selected-site cookies stay in Android's app storage; a temporary cookie file is deleted after each download. No passwords are collected by LocalSaver. To sign out, use the official site's sign-out control in the browser; clearing LocalSaver app storage removes its browser session and history.

Facebook story links open the in-app browser automatically. Play the specific video story before tapping download; the app tries to detect its video source. Instagram stories can use their individual story URL with an in-app session. Photo-only, expired, unavailable, or DRM-protected content cannot be downloaded. Some sites block embedded-browser sign-in or change their video delivery, so support varies. This does not promise every playable website video.

## Rebuild on Windows

The native Java app uses Android SDK 35, Java 17, Gradle 8.11.1, Android Gradle Plugin 8.9.2, and `youtubedl-android`/FFmpeg 0.18.1. Build helpers keep tools/cache/key local to `.tools`/`.gradle` and do not modify the system PATH.

```
python setup_tools.py
python build.py assembleRelease testDebugUnitTest
```

The APK is produced at `app/build/outputs/apk/release/app-release.apk`. Build.py creates a personal signing key at `.tools/localsaver.keystore`; back it up privately to sign future updates. Do not distribute the key. Android SDK use remains subject to Google's SDK license terms. SDK artifacts are downloaded from Google's repository and checked against its published hashes.

The committed browser-capture assets are included. `node prepare_assets.mjs` regenerates them from `tools/facebook-story.js` in this repository; the desktop project is not required. `setup_emulator.py` and the test helpers are optional and use a dedicated Android emulator on port 5580. They do not target a connected phone.

## References and open-source components

- Workflow reference: [InSaver's official Play listing](https://play.google.com/store/apps/details?id=instagram.video.downloader.story.saver.ig.insaver). LocalSaver has its own UI and source.
- Android engine and native dependencies: [youtubedl-android](https://github.com/yausername/youtubedl-android), [yt-dlp](https://github.com/yt-dlp/yt-dlp), [FFmpeg](https://ffmpeg.org/), [Python](https://www.python.org/), [QuickJS](https://bellard.org/quickjs/).
- [Android receive-shared-content documentation](https://developer.android.com/training/sharing/receive) and [shared media storage](https://developer.android.com/training/data-storage/shared/media).

See `THIRD_PARTY.md` for upstream licenses and corresponding-source links. This app does not contain IDM or InSaver proprietary code.
