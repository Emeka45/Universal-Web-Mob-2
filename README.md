# Universal Web-Mob 2

Universal Web-Mob 2 is a lightweight Android web workspace designed to make desktop-oriented web workflows practical from a phone.

## Focus

- ChatGPT web
- Cloudflare dashboard
- GitHub
- Desktop-style navigation and lightweight tabs
- Persistent web sessions through normal WebView cookies/storage
- File uploads through the Android document picker
- Downloads through Android system facilities
- Portrait and landscape support
- Universal purple U logo

## Deliberate exclusions

This project does not bundle GeckoView, Chromium/Titanium/Vanadium, a browser extension engine, Chrome Web Store support, autoplay authorization, credential extraction, or authentication bypass.

## Build

Use JDK 17 and Gradle 9.6 with Android Gradle Plugin 9.4.x. The project targets API 37 and supports Android API 26+.

Debug:
gradle assembleDebug

ABI-split debug:
gradle assembleDebug -PsplitApks=true

Release:
gradle assembleRelease

Bundle:
gradle bundleRelease
