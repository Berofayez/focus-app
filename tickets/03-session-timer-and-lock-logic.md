# 03 – Session timer and commitment lock logic

**Goal:** Start a session with a fixed end time. Once started it cannot be cancelled (except by an emergency pass, ticket 04).

**You will learn:** State machines, timers that survive app restarts (store the end timestamp, not a countdown), unit tests.

**Steps:**
1. `SessionState`: Planned -> Locked -> Completed / Abandoned.
2. Start action saves `lockedUntil` timestamp; remaining time is always computed from the clock.
3. A `LockManager` class with pure logic (no Android code) so it is easy to unit test.
4. Session screen shows the countdown and no cancel button while locked.
5. End-of-session screen: done / partly / stuck.

**Test:** Unit tests for the logic; manually start a 1-minute session, kill the app, reopen: the timer is still correct.

**Done when:** Lock state survives app kill and phone restart of the app.
