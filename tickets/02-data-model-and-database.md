# 02 – Data model and local database (Room)

**Goal:** Store goals and focus sessions on the phone.

**You will learn:** Room (entities, DAO, database), Kotlin data classes, Flow, ViewModel.

**Steps:**
1. Add Room + KSP dependencies.
2. Entities: `Goal(id, title, createdAt)`, `FocusSession(id, goalId, title, firstStep, plannedMinutes, allowedApps, allowedSites, status, startedAt, endedAt)`.
3. DAOs and the database class.
4. ViewModel exposing today's sessions as a Flow; Goals screen can add and list goals.

**Test:** Add a goal, close the app completely, reopen: the goal is still there.

**Done when:** Data persists across app restarts.
