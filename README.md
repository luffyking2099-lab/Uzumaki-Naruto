# Family Guardian — child-device starter (Android)

Companion starter to the parent web dashboard. Open this folder in Android Studio
(it expects the standard Gradle project layout).

## What's included

| Feature | How | File |
|---|---|---|
| Screen time | `UsageStatsManager`, polled every 15 min via WorkManager | `service/ScreenTimeWorker.kt` |
| App blocking | `AccessibilityService`, package name only — no content reading | `service/AppBlockerAccessibilityService.kt` |
| Location | `FusedLocationProviderClient` in a foreground service | `service/LocationUpdateService.kt` |
| Screen cast | `MediaProjection`, one consent dialog per session | `service/ScreenCastService.kt` + `ui/ScreenCastConsentActivity.kt` |

## Why there's no remote camera feature

Silent, remote camera access isn't included on purpose:

- Android and iOS both treat background camera access as spyware behavior and block or
  flag apps that do it — this would likely get a real app removed from the Play Store.
- Every feature above is built so the child's phone always shows something while it's
  active: a status-bar icon for location, Android's own screen-share indicator plus a
  notification for screen cast, and a settings toggle the child has to flip on for app
  blocking and usage access. That's a deliberate design line, not a technical limitation.

## What's stubbed and still needs work

- `ApiClient.kt` points at a placeholder URL — you need a real backend (the web dashboard
  needs one too) with endpoints for usage, location, pairing, and rule sync.
- `ScreenCastService` gets a live `MediaProjection` session but doesn't yet encode or
  transmit frames — that needs a `VirtualDisplay` + `MediaCodec` encoder streaming to
  whatever transport your backend uses (WebRTC is the usual choice for a live feed).
- Pairing code UI, rule-sync-from-server, and boot-time service restart are not built yet.

## Suggested next step

Stand up a minimal backend (even a single Node/Express service with a SQLite file) with
`/pair`, `/usage`, `/location`, and `/rules` endpoints, then wire `ApiClient` to it — that
unblocks testing the dashboard and the phone against real data instead of the demo values.
