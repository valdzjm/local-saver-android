# Validation — 2026-10-12

The app was installed and run on a dedicated Android 15/API 35 x86_64 emulator. The phone APK was built separately for ARM64/ARMv7 from the same app source. No physical phone was connected for testing.

| Download tested | Result measured from the saved MP4 |
| --- | --- |
| YouTube Short `TlRlDNHdmvk` | 1080×1920 H.264 + AAC, 24.17 seconds, 8,179,141 bytes |
| Facebook Reel `1822430045852437` | 720×1280 H.264 + AAC, 32.32 seconds, 5,893,386 bytes |
| Instagram Reel `DdgUlmZikAU` | 720×1280 H.264 + AAC, 21.11 seconds, 3,397,025 bytes |

All three jobs completed and published their videos through Android MediaStore. Saved files were read back from the emulator and inspected using FFprobe, confirming both video and audio tracks.

Additional checks passed:

- A text/plain Android share intent populated the video link.
- Android's notification permission prompt and foreground download notification worked.
- Deleting the test Instagram video, then tapping Redownload, produced a new completed video and MediaStore URI. The app was sent to the background during that redownload.
- Cancelling the longer YouTube sample ended in Stopped with no saved video. This was checked again after improving cancellation during engine process startup.
- A non-video page produced a persistent Error history entry.
- Facebook story sharing opened the in-app browser and the official Facebook login page.
- The per-job temporary download folder was empty after completion/cancellation/error.
- No AndroidRuntime crash was recorded during these checks.
- Nine JUnit tests passed: shared-link parsing, supported/spoofed host handling, HTTPS session scoping, site-specific cookie export, and rejection of cookie line injection.
- Release build and Android's release lint checks passed. APK Signature Scheme v2 verification passed; native packages include arm64-v8a and armeabi-v7a.

Signed-in Facebook/Instagram stories were not downloaded in the emulator: it had no signed-in account. Their browser flow and cookie handling are implemented, but live authenticated-story compatibility remains a phone/account test. Some websites may block embedded-browser login. This test does not establish support for every video or DRM stream.
