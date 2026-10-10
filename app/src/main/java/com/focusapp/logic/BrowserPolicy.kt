package com.focusapp.logic

import java.net.URI

// Pure logic, no Android/WebView dependencies, so domain and video-URL
// matching can be unit tested directly.
object BrowserPolicy {
    // Blocked outright, regardless of the session's allow-list (ticket step 5).
    private val blockedHosts = setOf(
        "youtube.com",
        "youtu.be",
        "netflix.com",
        "vimeo.com",
        "dailymotion.com",
        "twitch.tv",
        "tiktok.com",
    )

    private val videoExtensions = setOf(".mp4", ".webm", ".m3u8", ".mpd", ".ts", ".mov", ".avi", ".flv")

    fun hostOf(url: String): String? = try {
        URI(url).host
    } catch (malformed: Exception) {
        null
    }

    fun isBlockedHost(host: String?): Boolean = host != null && blockedHosts.any { matchesDomain(host, it) }

    fun isAllowedHost(host: String?, allowedSites: List<String>): Boolean =
        host != null && allowedSites.any { matchesDomain(host, it) }

    fun looksLikeVideoUrl(url: String): Boolean {
        val path = url.substringBefore('?').lowercase()
        return videoExtensions.any { path.endsWith(it) }
    }

    fun canNavigate(url: String, allowedSites: List<String>): Boolean {
        val host = hostOf(url)
        return !isBlockedHost(host) && isAllowedHost(host, allowedSites)
    }

    private fun matchesDomain(host: String, domain: String): Boolean =
        host.equals(domain, ignoreCase = true) || host.endsWith(".$domain", ignoreCase = true)
}
