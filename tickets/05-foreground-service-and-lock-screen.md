# 05 – Foreground service and lock screen

**Goal:** The session keeps running reliably in the background, with a persistent notification.

**You will learn:** Foreground services, notification channels, Android permissions, lifecycle.

**Steps:**
1. `SessionService` (foreground service) holds the active session and posts a notification with remaining time.
2. Request the notification permission (Android 13+).
3. Service starts on session start and stops on completion/abandon.
4. Midpoint check-in notification ("still on track?").

**Test:** Start a session, switch apps, wait: notification keeps ticking and the session ends on time.

**Done when:** The session is not killed when you leave the app.
