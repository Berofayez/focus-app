package com.focusapp.ui.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusapp.data.FocusAppDatabase
import com.focusapp.data.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val goalDao = FocusAppDatabase.getInstance(application).goalDao()

    val goals: Flow<List<Goal>> = goalDao.getAll()

    fun addGoal(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            goalDao.insert(Goal(title = title.trim(), createdAt = System.currentTimeMillis()))
        }
    }
}
