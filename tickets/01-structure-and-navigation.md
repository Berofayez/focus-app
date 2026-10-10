# 01 – App structure, theme and navigation

**Goal:** A calm, minimal UI shell with three screens: Today, Goals, Session. Home shows only "Now" and "Next" (ADHD-friendly: one thing at a time).

**You will learn:** Composables, state, Material 3 theming, Navigation Compose.

**Steps:**
1. Add the Navigation Compose dependency.
2. Create a theme (low-stimulation colors, large readable text).
3. Create `TodayScreen`, `GoalsScreen`, `SessionScreen` with placeholder content and a bottom bar or simple buttons.
4. Wire navigation in `MainActivity`.

**Test:** Navigate between all three screens on the phone; rotating the phone does not crash.

**Done when:** Screens exist, navigation works, code is split into small files (one screen per file).
