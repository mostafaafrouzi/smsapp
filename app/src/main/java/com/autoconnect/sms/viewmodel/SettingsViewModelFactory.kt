package com.autoconnect.sms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.prefs.PreferencesManager

class SettingsViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferencesManager, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
