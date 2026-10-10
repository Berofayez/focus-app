package com.focusapp.ui.session

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusapp.BuildConfig
import com.focusapp.data.EmergencyPassLog
import com.focusapp.data.FocusAppDatabase
import com.focusapp.data.FocusSession
import com.focusapp.data.Goal
import com.focusapp.data.SessionOutcome
import com.focusapp.data.SessionStatus
import com.focusapp.logic.EmergencyPassPolicy
import com.focusapp.logic.LockManager
import com.focusapp.service.SessionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FocusAppDatabase.getInstance(application)
    private val focusSessionDao = database.focusSessionDao()
    private val goalDao = database.goalDao()
    private val emergencyPassLogDao = database.emergencyPassLogDao()

    val lockManager = LockManager()

    // Shortened wait in debug builds only, so the end-to-end flow can be tested
    // manually without actually waiting 15 minutes.
    val emergencyPassPolicy = EmergencyPassPolicy(waitMinutes = if (BuildConfig.DEBUG) 1 else 15)

    val goals: Flow<List<Goal>> = goalDao.getAll()
    val currentLockedSession: Flow<FocusSession?> = focusSessionDao.getCurrentLocked()

    val emergencyPassesUsedThisWeek: Flow<Int> = emergencyPassLogDao
        .getSince(emergencyPassPolicy.currentWeekStart())
        .map { it.size }

    // Creating and starting are the same action for now: there is no separate
    // planning step yet (that arrives with the AI planner in tickets 08/09).
    fun startSession(
        goalId: Long,
        title: String,
        firstStep: String,
        plannedMinutes: Int,
        allowedSites: List<String>,
    ) {
        viewModelScope.launch {
            focusSessionDao.insert(
                FocusSession(
                    goalId = goalId,
                    title = title.trim(),
                    firstStep = firstStep.trim(),
                    plannedMinutes = plannedMinutes,
                    allowedApps = emptyList(),
                    allowedSites = allowedSites,
                    status = SessionStatus.LOCKED,
                    startedAt = System.currentTimeMillis(),
                    endedAt = null,
                ),
            )
            ContextCompat.startForegroundService(
                getApplication(),
                Intent(getApplication(), SessionService::class.java),
            )
        }
    }

    fun completeSession(outcome: SessionOutcome) {
        viewModelScope.launch {
            val session = currentLockedSession.first() ?: return@launch
            focusSessionDao.update(
                session.copy(
                    status = SessionStatus.COMPLETED,
                    endedAt = System.currentTimeMillis(),
                    outcome = outcome,
                ),
            )
        }
    }

    // Starts the wait; does not consume the pass yet. The caller is expected to
    // have already checked emergencyPassesUsedThisWeek against the policy.
    fun requestEmergencyExit(session: FocusSession, reason: String) {
        viewModelScope.launch {
            focusSessionDao.update(
                session.copy(
                    emergencyExitRequestedAt = System.currentTimeMillis(),
                    emergencyExitReason = reason.trim(),
                ),
            )
        }
    }

    fun cancelEmergencyExit(session: FocusSession) {
        viewModelScope.launch {
            focusSessionDao.update(
                session.copy(emergencyExitRequestedAt = null, emergencyExitReason = null),
            )
        }
    }

    // Called once the wait has run out: the session is abandoned and the pass consumed.
    fun consumeEmergencyExit(session: FocusSession) {
        val requestedAt = session.emergencyExitRequestedAt ?: return
        viewModelScope.launch {
            focusSessionDao.update(
                session.copy(status = SessionStatus.ABANDONED, endedAt = System.currentTimeMillis()),
            )
            emergencyPassLogDao.insert(
                EmergencyPassLog(
                    sessionId = session.id,
                    reason = session.emergencyExitReason.orEmpty(),
                    usedAt = requestedAt,
                ),
            )
        }
    }
}
