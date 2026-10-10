# 06 – App-blocking overlay

**Goal:** During a locked session, any app not on the allow-list is covered by a "back to your task" screen.

**You will learn:** Accessibility Service (or UsageStats), overlay permission, Android security model and why these permissions exist.

**Steps:**
1. Explain and request the needed permissions with clear in-app screens (user grants them in system settings).
2. Detect the foreground app.
3. If not allowed, bring our app to the front or show an overlay with the task title and first step.
4. Always allow: Phone/Dialer, chosen messaging contacts app, Maps, banking, system settings (so you can never get stuck), and our app.
5. Preset block list: YouTube, Netflix, other video apps, other browsers.

**Test (physical phone only):** Start a session; open YouTube -> overlay appears; open the Phone app -> works.

**Done when:** Blocked apps are reliably covered, always-allowed apps are untouched, and the lock ends cleanly when the session ends.
