package com.focusapp.ui.browser

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.focusapp.logic.BrowserPolicy

// A restricted browser: text and images load, video doesn't, and navigation
// is limited to the session's allowed sites. Only reachable from within a
// locked session (see SessionScreen), never as a general-purpose tab.
@Composable
fun FocusBrowserScreen(allowedSites: List<String>, onExit: () -> Unit) {
    BackHandler(onBack = onExit)

    var addressInput by remember {
        mutableStateOf(allowedSites.firstOrNull()?.let { "https://$it" } ?: "")
    }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (allowedSites.isEmpty()) {
            Text(
                "No allowed sites were set for this session.",
                modifier = Modifier.padding(16.dp),
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    singleLine = true,
                    label = { Text("Address") },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val url = normalizeUrl(addressInput)
                        if (BrowserPolicy.canNavigate(url, allowedSites)) {
                            webView?.loadUrl(url)
                        } else {
                            webView?.loadDataWithBaseURL(
                                null,
                                FocusWebViewClient.blockedPageHtml(BrowserPolicy.hostOf(url)),
                                "text/html",
                                "utf-8",
                                null,
                            )
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text("Go")
                }
            }

            AndroidView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = true
                        webViewClient = FocusWebViewClient(allowedSites)
                        webView = this
                        loadUrl(normalizeUrl(addressInput))
                    }
                },
            )
        }

        Button(onClick = onExit, modifier = Modifier.padding(8.dp)) {
            Text("Back to task")
        }
    }
}

private fun normalizeUrl(input: String): String =
    if (input.startsWith("http://") || input.startsWith("https://")) input else "https://$input"
