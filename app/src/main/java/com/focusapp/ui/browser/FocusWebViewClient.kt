package com.focusapp.ui.browser

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.focusapp.logic.BrowserPolicy
import java.io.ByteArrayInputStream

// Restricts navigation to the session's allowed sites (youtube.com and other
// streaming domains are blocked outright, regardless of the allow-list), and
// blocks obvious video file requests at the network level. The JS injected in
// onPageFinished catches players that don't show up as a single video URL.
class FocusWebViewClient(private val allowedSites: List<String>) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        if (!BrowserPolicy.canNavigate(url, allowedSites)) {
            view.loadDataWithBaseURL(null, blockedPageHtml(BrowserPolicy.hostOf(url)), "text/html", "utf-8", null)
            return true
        }
        return false
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        val host = BrowserPolicy.hostOf(url)
        if (BrowserPolicy.looksLikeVideoUrl(url) || BrowserPolicy.isBlockedHost(host)) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }
        return null
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        view.evaluateJavascript(STRIP_VIDEO_JS, null)
    }

    companion object {
        fun blockedPageHtml(host: String?): String = """
            <html><body style="font-family:sans-serif;text-align:center;padding-top:40%;">
            <h2>Not part of this task</h2>
            <p>${host ?: "This site"} isn't on the allowed list for this session.</p>
            </body></html>
        """.trimIndent()

        // Removes <video> elements and known video-embed iframes, and re-runs
        // on later DOM changes so lazily-loaded or SPA-rendered players are
        // caught too.
        private val STRIP_VIDEO_JS = """
            (function() {
                function stripVideos() {
                    document.querySelectorAll('video').forEach(function(v) { v.remove(); });
                    document.querySelectorAll('iframe').forEach(function(f) {
                        var src = (f.src || '').toLowerCase();
                        if (src.indexOf('youtube.com/embed') !== -1 ||
                            src.indexOf('player.vimeo.com') !== -1 ||
                            src.indexOf('dailymotion.com/embed') !== -1) {
                            f.remove();
                        }
                    });
                }
                stripVideos();
                var observer = new MutationObserver(function() { stripVideos(); });
                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                }
            })();
        """.trimIndent()
    }
}
