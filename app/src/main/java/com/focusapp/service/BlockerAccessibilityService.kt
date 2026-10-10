package com.focusapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.focusapp.MainActivity
import com.focusapp.data.FocusAppDatabase
import com.focusapp.data.FocusSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Watches which app is in the foreground. While a session is locked, any app
// not on the allow-list gets covered by bringing our own app back to the
// front, so the session screen (with its countdown and no cancel button)
// is what you actually see.
class BlockerAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile private var lockedSession: FocusSession? = null
    private lateinit var allowedPackages: Set<String>

    override fun onServiceConnected() {
        super.onServiceConnected()
        allowedPackages = AllowList.allowedPackages(this)

        val focusSessionDao = FocusAppDatabase.getInstance(applicationContext).focusSessionDao()
        serviceScope.launch {
            focusSessionDao.getCurrentLocked().collectLatest { lockedSession = it }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (lockedSession == null) return
        val foregroundPackage = event?.packageName?.toString() ?: return
        if (foregroundPackage in allowedPackages) return
        bringAppToFront()
    }

    private fun bringAppToFront() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun isEnabled(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
        }
    }
}
