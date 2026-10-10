package com.focusapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.focusapp.MainActivity
import com.focusapp.R
import com.focusapp.data.FocusAppDatabase
import com.focusapp.data.FocusSession
import com.focusapp.logic.LockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Holds the active locked session in the background: posts a persistent,
// ticking notification and a midpoint check-in, and stops itself the moment
// the session is no longer LOCKED in the database (completed/abandoned).
class SessionService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lockManager = LockManager()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        val focusSessionDao = FocusAppDatabase.getInstance(applicationContext).focusSessionDao()
        serviceScope.launch {
            focusSessionDao.getCurrentLocked().collectLatest { session ->
                if (session == null) {
                    stopSelf()
                } else {
                    startForeground(ONGOING_NOTIFICATION_ID, buildOngoingNotification(session))
                    awaitMidpointAndCheckIn(session)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun awaitMidpointAndCheckIn(session: FocusSession) {
        val startedAt = session.startedAt ?: return
        val lockedUntil = lockManager.lockedUntil(startedAt, session.plannedMinutes)
        val midpoint = startedAt + (lockedUntil - startedAt) / 2
        val delayMillis = midpoint - System.currentTimeMillis()
        if (delayMillis > 0) {
            delay(delayMillis)
            postCheckInNotification(session)
        }
    }

    private fun buildOngoingNotification(session: FocusSession): Notification {
        val lockedUntil = lockManager.lockedUntil(session.startedAt ?: 0L, session.plannedMinutes)
        return NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(session.title)
            .setContentText(session.firstStep)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(lockedUntil)
            .setContentIntent(openAppPendingIntent())
            .build()
    }

    private fun postCheckInNotification(session: FocusSession) {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(this, CHECKIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Still on track?")
            .setContentText(session.title)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent())
            .build()
        NotificationManagerCompat.from(this).notify(CHECKIN_NOTIFICATION_ID, notification)
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ONGOING_CHANNEL_ID, "Active session", NotificationManager.IMPORTANCE_LOW),
        )
        manager.createNotificationChannel(
            NotificationChannel(CHECKIN_CHANNEL_ID, "Session check-in", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    companion object {
        private const val ONGOING_CHANNEL_ID = "session_ongoing"
        private const val CHECKIN_CHANNEL_ID = "session_checkin"
        private const val ONGOING_NOTIFICATION_ID = 1
        private const val CHECKIN_NOTIFICATION_ID = 2
    }
}
