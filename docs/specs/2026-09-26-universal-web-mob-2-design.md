# Universal Web-Mob 2 — Design Specification

Date: 2026-09-26
Repository: Emeka45/Universal-Web-Mob-2

## 1. Goal

Build a lightweight Android web-workspace that gives a phone a practical PC-style web interface for working with services such as ChatGPT, Cloudflare, and GitHub.

This is not intended to be a full browser engine or a replacement for desktop Chrome/Firefox.

## 2. Architecture

- Native Android application shell.
- Android WebView for page rendering and JavaScript.
- Custom desktop-style UI around the WebView.
- Persistent WebView cookies/session data so legitimate authenticated sessions can survive app restarts.
- Android file picker for uploads and DownloadManager/system download handling where appropriate.
- No GeckoView.
- No bundled Chromium/Titanium/Vanadium engine.
- No browser extension engine or Chrome Web Store subsystem.

## 3. User Interface

- Top navigation row: Back, Forward, Reload, Home, address/search field, menu.
- Tab strip: multiple lightweight web tabs plus a new-tab action.
- Main content area: WebView.
- Desktop-oriented presentation where websites support it, while remaining usable on small screens.
- Adaptive portrait and landscape layouts; landscape is optimized but portrait remains supported.
- Touch controls remain practical on low-resolution/low-memory devices.

## 4. Core Web Capabilities

- Normal HTTPS web navigation.
- JavaScript enabled.
- DOM storage enabled.
- Cookies enabled.
- File upload through Android's document picker.
- File downloads through Android/system facilities.
- External intents for links/actions that cannot or should not be handled inside the WebView.
- Navigation/error handling with a useful retry path.
- No attempt to bypass authentication, CAPTCHA, account restrictions, or service security.

## 5. Workflow Focus

First-class workflow targets:
1. ChatGPT web
2. Cloudflare dashboard
3. GitHub

The app should make these sites convenient to access from a phone, but it cannot itself grant ChatGPT access to an account or connector that the platform does not support on mobile.

## 6. Branding

Universal Web-Mob 2 must retain the existing Universal logo identity rather than introducing an unrelated logo.

The existing logo uses a purple circular mark with a white stacked geometric U-style symbol. The new project should reuse/adapt that vector asset for the launcher icon, app identity/splash treatment, and appropriate in-app branding.

Branding must remain lightweight and should not require large raster assets.

## 7. Performance

Target low-end Android devices, including the Redmi A1.

Priorities:
- minimal dependencies
- no unnecessary background services
- no large native browser-engine packages
- avoid memory-heavy tab behavior
- responsive UI on constrained hardware

## 8. Build and Distribution

GitHub Actions should provide:
- debug APK
- release APK
- Android App Bundle (AAB)
- architecture-appropriate APK outputs when splitting is useful

Build configuration must be reproducible and should not require a paid service.

## 9. Verification

Before declaring a build successful, verify:
- Gradle compilation succeeds.
- APK artifact exists and is identifiable.
- Package/application ID is correct.
- Debug/release artifacts are distinguished correctly.
- No GeckoView dependency remains.
- No extension/Web Store/autoplay-authorization subsystem is introduced.
- The logo resource is present.
- GitHub Actions artifact upload succeeds.

## 10. Out of Scope

- Full browser-engine development.
- GeckoView.
- Chromium bundling.
- Browser extensions.
- Chrome Web Store support.
- Automatic account linking or credential extraction.
- Circumventing service authentication or access controls.
- AI model hosting inside the app.

## 11. Success Criteria

Universal Web-Mob 2 is successful when a user can install it on a low-end Android phone, open desktop-oriented web services, navigate between multiple lightweight tabs, authenticate normally, upload/download files, and use ChatGPT/Cloudflare/GitHub workflows through the web without the heavyweight browser-engine dependencies that caused problems in the previous project.
