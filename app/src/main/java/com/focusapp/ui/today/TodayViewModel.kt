package com.focusapp.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.focusapp.data.FocusAppDatabase
import com.focusapp.data.FocusSession
import kotlinx.coroutines.flow.Flow

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val focusSessionDao = FocusAppDatabase.getInstance(application).focusSessionDao()

    val activeSessions: Flow<List<FocusSession>> = focusSessionDao.getActive()
}
