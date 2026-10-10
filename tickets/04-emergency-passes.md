# 04 – Emergency passes

**Goal:** 1-2 emergency exits per week. Each needs a typed reason and a 15-minute wait. This prevents impulse quitting but lets you handle real emergencies.

**You will learn:** DataStore/Room for counters, week boundaries with `java.time`, delayed actions.

**Steps:**
1. Store passes used this week (resets Monday 00:00 local time).
2. "Emergency exit" button on the locked session screen -> reason text -> starts a 15-min countdown (visible, cancellable).
3. When the countdown finishes, the session ends as Abandoned and the pass is consumed.
4. Log reasons for the weekly review.

**Test:** Unit tests for the weekly reset and the 15-minute rule; manually use a pass with a shortened wait (debug flag).

**Done when:** Passes cannot be bypassed by restarting the app.
