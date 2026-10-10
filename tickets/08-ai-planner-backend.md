# 08 – AI planner backend

**Goal:** Turn a goal into a roadmap, a weekly plan and daily sessions using the Claude API, with the API key kept off the phone.

**You will learn:** Calling an LLM API, structured JSON output, why API keys must never ship inside an app, simple HTTP in Kotlin.

**Steps:**
1. Decide the backend (small serverless function) together; Claude explains the options first.
2. Define the JSON schema: milestones, weekly outcomes, daily sessions (title, first step, minutes, allowed apps/sites).
3. Write the system prompt (ADHD-friendly: small steps, max 4 sessions/day, no guilt, hard tasks in high-energy hours).
4. Android side: Retrofit/Ktor client, parse JSON into the Room entities from ticket 02.

**Test:** Generate plans for 3 sample goals; every response parses and fits the schema.

**Done when:** A goal produces today's sessions in the app.
