package com.focusapp.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserPolicyTest {

    @Test
    fun `hostOf extracts the host from a url`() {
        assertEquals("www.bbc.com", BrowserPolicy.hostOf("https://www.bbc.com/news/article-1"))
    }

    @Test
    fun `hostOf returns null for a malformed url`() {
        assertNull(BrowserPolicy.hostOf("not a url"))
    }

    @Test
    fun `isAllowedHost matches the exact domain`() {
        assertTrue(BrowserPolicy.isAllowedHost("bbc.com", listOf("bbc.com")))
    }

    @Test
    fun `isAllowedHost matches a subdomain of an allowed domain`() {
        assertTrue(BrowserPolicy.isAllowedHost("m.bbc.com", listOf("bbc.com")))
    }

    @Test
    fun `isAllowedHost rejects an unrelated domain`() {
        assertFalse(BrowserPolicy.isAllowedHost("example.com", listOf("bbc.com")))
    }

    @Test
    fun `isAllowedHost rejects a lookalike domain that merely contains the allowed one`() {
        assertFalse(BrowserPolicy.isAllowedHost("notbbc.com", listOf("bbc.com")))
    }

    @Test
    fun `isBlockedHost catches youtube and its short domain`() {
        assertTrue(BrowserPolicy.isBlockedHost("www.youtube.com"))
        assertTrue(BrowserPolicy.isBlockedHost("youtu.be"))
    }

    @Test
    fun `canNavigate blocks youtube even if it were on the allow-list`() {
        val url = "https://www.youtube.com/watch?v=123"
        assertFalse(BrowserPolicy.canNavigate(url, allowedSites = listOf("youtube.com")))
    }

    @Test
    fun `canNavigate allows a host on the allow-list`() {
        assertTrue(BrowserPolicy.canNavigate("https://bbc.com/news", allowedSites = listOf("bbc.com")))
    }

    @Test
    fun `canNavigate rejects a host not on the allow-list`() {
        assertFalse(BrowserPolicy.canNavigate("https://example.com", allowedSites = listOf("bbc.com")))
    }

    @Test
    fun `looksLikeVideoUrl matches common video file extensions regardless of case or query string`() {
        assertTrue(BrowserPolicy.looksLikeVideoUrl("https://cdn.example.com/clip.MP4?token=abc"))
        assertTrue(BrowserPolicy.looksLikeVideoUrl("https://cdn.example.com/stream.m3u8"))
    }

    @Test
    fun `looksLikeVideoUrl does not flag a normal page or image`() {
        assertFalse(BrowserPolicy.looksLikeVideoUrl("https://example.com/article"))
        assertFalse(BrowserPolicy.looksLikeVideoUrl("https://example.com/photo.jpg"))
    }
}
