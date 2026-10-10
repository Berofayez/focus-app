# 00 – Open the project and run it on your phone

**Goal:** Confirm the empty project builds and shows "Focus App is alive" on your phone.

**You will learn:** Gradle sync, running on a real device, USB debugging.

**Steps (you do these):**
1. Android Studio -> File -> Open -> choose `~/Documents/focus-app`.
2. Wait for Gradle sync (first time downloads Gradle 8.9 and libraries; can take several minutes).
3. If Android Studio asks to use a JDK, pick its bundled one (JetBrains Runtime 17+).
4. On your phone: Settings -> About phone -> tap Build number 7 times -> Developer options -> enable USB debugging. Plug in via USB and accept the prompt.
5. Pick your phone in the device dropdown and press Run (green triangle).

**Test:** The app opens and shows the text "Focus App is alive".

**Done when:** It runs on your phone. If sync or build fails, paste the error to Claude.
