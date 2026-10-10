package com.focusapp.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.provider.Telephony
import android.telecom.TelecomManager

// Apps you can always reach, even during a locked session, so you can never
// get stuck: our own app, the phone/dialer, your chosen messaging app, maps,
// and system settings. Everything else is blocked by default - there is no
// separate hardcoded block list, since anything not on this allow-list
// already counts as blocked (that covers YouTube, Netflix, other browsers, etc).
object AllowList {
    fun allowedPackages(context: Context): Set<String> {
        val pm = context.packageManager
        val packages = mutableSetOf(context.packageName, "com.android.systemui")

        resolveHomePackage(pm)?.let { packages.add(it) }

        val telecomManager = context.getSystemService(TelecomManager::class.java)
        telecomManager?.defaultDialerPackage?.let { packages.add(it) }

        Telephony.Sms.getDefaultSmsPackage(context)?.let { packages.add(it) }

        resolvePackageForIntent(pm, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")))?.let { packages.add(it) }

        packages.add("com.android.settings")

        return packages
    }

    private fun resolveHomePackage(pm: PackageManager): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return resolvePackageForIntent(pm, intent)
    }

    private fun resolvePackageForIntent(pm: PackageManager, intent: Intent): String? =
        pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
}
