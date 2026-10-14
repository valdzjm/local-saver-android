# Open-source notices

LocalSaver's source is provided under GPL-3.0; see `LICENSE`. No IDM or InSaver binaries/assets are included. The app bundles upstream open-source downloader and media components through these Maven dependencies:

- `io.github.junkfood02.youtubedl-android:library:0.18.1`
- `io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1`

Upstream source, licenses, dependency builds, and release materials:

- [youtubedl-android source](https://github.com/yausername/youtubedl-android) and [GPL-3.0 license](https://github.com/yausername/youtubedl-android/blob/master/LICENSE).
- [yt-dlp source and license notices](https://github.com/yt-dlp/yt-dlp). Its engine is also updated from the official yt-dlp stable release repository during app use.
- [FFmpeg source and licensing](https://ffmpeg.org/legal.html); native builds are supplied by the Android wrapper dependency. Build recipes and dependency details are available in that wrapper's source repository.
- [Python source and license](https://github.com/python/cpython/blob/main/LICENSE).
- [QuickJS source and license](https://github.com/bellard/quickjs/blob/master/LICENSE).
- [Kotlin](https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt), [Jackson](https://github.com/FasterXML/jackson-core/blob/2.18/LICENSE), and [Apache Commons IO](https://github.com/apache/commons-io/blob/master/LICENSE.txt) are transitive dependencies; their upstream licenses and packaged metadata apply.

The source archive contains LocalSaver's app code, configuration, and committed Facebook browser-capture scripts. Signing keys, build caches, downloaded SDK/tool archives, and browser/account data are excluded. Google's Android SDK/JDK/Gradle tools are build dependencies and are not bundled in the phone APK.
