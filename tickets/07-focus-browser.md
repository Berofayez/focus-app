# 07 – Focus Browser (video blocked)

**Goal:** A built-in browser that shows text and images but no video, restricted to the session's allowed sites.

**You will learn:** WebView, request interception, injecting JavaScript, domain matching.

**Steps:**
1. Compose screen hosting a `WebView` with address bar limited to allowed sites.
2. `shouldInterceptRequest`: block known video domains and video/media MIME types.
3. Inject a script that removes `<video>`, player iframes and autoplay embeds (and re-runs on page changes).
4. Block navigation outside the allow-list; show a friendly "not part of this task" page.
5. Block `youtube.com` and common streaming sites outright.

**Test:** Open a news article with an embedded video: text and images show, video is gone. Open youtube.com: blocked.

**Done when:** Reading works; videos do not play on a set of test pages we choose together.
