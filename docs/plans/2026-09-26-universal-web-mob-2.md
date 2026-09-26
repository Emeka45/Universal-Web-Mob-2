# Universal Web-Mob 2 Implementation Plan

> **For agentic workers:** Use the host's available task-by-task implementation workflow. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a lightweight Android WebView-based desktop-style web workspace for ChatGPT, Cloudflare, GitHub, uploads/downloads, and normal authenticated web use on low-end Android devices.

**Architecture:** A native Android shell will host one or more lightweight WebView instances behind a desktop-style navigation and tab UI. Persistent cookies/storage will preserve legitimate sessions, while Android intents handle file selection, downloads, and external links. The implementation deliberately avoids GeckoView, bundled browser engines, extensions, Web Store support, autoplay authorization, and authentication bypass.

**Tech Stack:** Kotlin, Android SDK, Android WebView, XML layouts/resources, Gradle Kotlin DSL, GitHub Actions, JUnit/Android instrumentation tests where practical.

## Global Constraints
- Android WebView for rendering and JavaScript.
- HTTPS navigation, JavaScript, DOM storage, cookies, file uploads, downloads, and external intents.
- Back, Forward, Reload, Home, address/search, menu, lightweight tabs, and new-tab controls.
- Portrait and landscape, with landscape optimized.
- Low-end device target including Redmi A1; minimal dependencies and memory use.
- Reuse the existing Universal purple circular logo with white geometric U-style mark.
- No GeckoView, bundled Chromium/Titanium/Vanadium, browser extensions, Chrome Web Store, autoplay authorization, credential extraction, authentication bypass, CAPTCHA bypass, or AI model hosting.
- GitHub Actions must provide debug APK, release APK, AAB, and useful architecture-specific APK outputs.
- Verification must confirm compilation, identifiable artifacts, correct application ID, debug/release distinction, absence of forbidden subsystems, logo presence, and artifact upload.

---

### Task 1: Android project foundation and branding

**Files:** Create settings.gradle.kts, build.gradle.kts, gradle.properties, app/build.gradle.kts, app/src/main/AndroidManifest.xml, MainActivity.kt, strings.xml, themes.xml, ic_logo.xml, launcher resources, and a package configuration unit test.

**Interfaces:** The foundation produces application ID `com.coeric.universalwebmob2`, the launcher/app logo resource, and the activity entry point.

- [ ] Add a focused test asserting application ID, minimum SDK, and logo resource.
- [ ] Run the focused test before implementation and confirm the expected missing-resource/configuration failure.
- [ ] Implement the minimal Android project with a stable Android Gradle Plugin/Gradle combination, consistent Java/Kotlin versions, only required AndroidX dependencies, internet permission, launcher activity, and the existing lightweight vector logo. Do not add any browser engine dependency.
- [ ] Run the focused test and debug build; expect the test to pass and a debug APK to exist.
- [ ] Commit the passing foundation as `feat: add Web-Mob 2 Android foundation`.

### Task 2: Desktop-style Web workspace, navigation, tabs, and web capabilities

**Files:** Create activity_main.xml, view_tab.xml, main_menu.xml, WebTab.kt, WebTabManager.kt, WebClient.kt, WebChromeClient.kt, WebDownloadHandler.kt, WebIntentHandler.kt, WebSettingsFactory.kt, plus unit/instrumentation tests; modify MainActivity.kt.

**Interfaces:** `WebSettingsFactory.create(context)`, `WebTabManager.createTab(url)`, `selectTab(id)`, `closeTab(id)`, `activeTab()`, `WebDownloadHandler.handle(request)`, and `WebIntentHandler.handle(uri)`.

- [ ] Add failing tab-manager tests for unique IDs, selection, active-tab closure, and last-tab replacement.
- [ ] Run focused tests and confirm they fail because the manager is absent.
- [ ] Implement the desktop-style top bar, lightweight tab strip, WebView settings, persistent cookies/DOM storage, URL/address handling, Android document-picker file chooser, system downloads, external intents, navigation errors with retry, and bounded WebView lifecycle. Keep normal HTTPS pages inside WebView and do not weaken security settings.
- [ ] Run unit tests and expect tab lifecycle tests to pass.
- [ ] Run debug compilation and instrumentation checks where available; expect the navigation shell to launch in portrait and landscape.
- [ ] Commit as `feat: add desktop web workspace and lightweight tabs`.

### Task 3: Workflow shortcuts, persistence, and GitHub Actions distribution

**Files:** Create StartPages.kt, .github/workflows/android.yml, README.md, relevant backup/config resources, and shortcut tests; modify MainActivity.kt and strings.xml.

**Interfaces:** `StartPages.CHATGPT`, `StartPages.CLOUDFLARE`, `StartPages.GITHUB`, and `StartPages.homeUrl` provide canonical HTTPS workflow destinations.

- [ ] Add failing tests verifying the three workflow destinations and home behavior.
- [ ] Run focused tests and confirm the constants/bindings are missing.
- [ ] Add ChatGPT, Cloudflare, and GitHub shortcuts without automatic authentication or account linking. Preserve legitimate WebView cookies/storage, do not store raw passwords, and add menu actions for home/new tab/close/retry/external open. Add GitHub Actions to build tests, debug APKs, release artifacts when signing secrets are configured, AAB, and useful ABI outputs. Keep release signing secret-driven and never commit credentials.
- [ ] Run shortcut tests and expect them to pass.
- [ ] Run full local verification, inspect APK/AAB names and sizes, and trigger GitHub Actions. Expect successful debug artifact upload; release signing must succeed when configured or skip signing clearly without fabricating credentials.
- [ ] Commit as `feat: add workflow shortcuts and CI artifacts`.

### Task 4: End-to-end verification and low-end-device hardening

**Files:** Only affected implementation files discovered during verification, plus regression tests.

**Interfaces:** The complete app produces verified debug APK, release output where signing is configured, AAB, and architecture-specific outputs where enabled.

- [ ] Add regression coverage for Back/Forward/Reload, navigation retry, file chooser URI handling, external schemes, tab closure/resource release, orientation recreation, logo resolution, and absence of forbidden dependency strings.
- [ ] Run focused regression tests before hardening and record only actual regressions.
- [ ] Fix measured issues while keeping work off the main thread where appropriate, releasing closed WebViews, limiting retained tabs, and avoiding background services. Confirm no extension/Web Store/autoplay/GeckoView/credential-bypass code exists.
- [ ] Run all unit and instrumentation tests.
- [ ] Run full build/verification, inspect application ID, debug/release labels, logo, APK/AAB existence, and GitHub Actions artifact upload. On an available device, exercise ChatGPT, Cloudflare, GitHub, file upload/download, tabs, rotation, and retry.
- [ ] Commit verified changes as `test: verify Web-Mob 2 build and web workflows`.

## Unresolved externally observable decisions
- Release signing credentials and the exact release keystore are not specified; signing must remain secret-driven.
- The specification does not mandate a home URL; the plan uses a lightweight internal start page with the three workflow shortcuts.
- The specification does not mandate a search provider; plain-text address-bar searches should use a neutral configurable search URL.