package com.autoconnect.sms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.prefs.PreferencesManager

class LogsViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            return LogsViewModel(preferencesManager, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
